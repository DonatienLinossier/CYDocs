package com.example.demo.services;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import org.springframework.stereotype.Service;
import com.example.demo.repositories.UserRepository;
import com.cyFramework.core.Acteur;
import com.cyFramework.core.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DocumentService extends Acteur {

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    private final UserRepository userRepository;

    private final Map<String, CompletableFuture<Long>> pendingValidations = new ConcurrentHashMap<>();

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository, UserRepository userRepository) {
        super("document-management");
        this.repo = repo;
        this.accesRepository = accesRepository;
        this.userRepository = userRepository;
        this.demarrer();
    }

    @Override
    public void recevoirMessage(Message message) {
        String contenuBrut = message.getContenu();
        if (contenuBrut == null) return;

        // Decoupage sécurisé
        String[] parts = contenuBrut.split(":", 3);
        String action = parts[0];

        switch (action) {
            case "PING" -> {
                this.getLogger().info("Health check : PING reçu → OK");
            }

            case "TOKEN_VALIDATED" -> {
                if (parts.length >= 3) {
                    String token = parts[1];
                    String userIdStr = parts[2];

                    // On retrouve la promesse grâce au token
                    CompletableFuture<Long> future = pendingValidations.remove(token);
                    if (future != null) {
                        try {
                            future.complete(Long.parseLong(userIdStr));
                            this.getLogger().info("Token validé via message pour UserID=" + userIdStr);
                        } catch (NumberFormatException e) {
                            future.completeExceptionally(new RuntimeException("ID utilisateur invalide reçu"));
                        }
                    }
                }
            }

            case "TOKEN_INVALID" -> {
                if (parts.length >= 2) {
                    String token = parts[1];
                    CompletableFuture<Long> future = pendingValidations.remove(token);
                    if (future != null) {
                        future.completeExceptionally(new RuntimeException("Token invalide ou expiré"));
                    }
                }
            }

            default -> {
                this.getLogger().warn("Action inconnue : " + action);
            }
        }
    }

    public Long validateTokenViaActor(String token) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        // On enregistre la demande
        pendingValidations.put(token, future);

      
        this.envoyerMessage("user-management", new Message("document-management", "user-management", "TOKEN_REQUEST:" + token));

        try {
            // On attend la réponse max 5 secondes
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingValidations.remove(token); // Nettoyage si timeout
            this.getLogger().error("Erreur validation asynchrone : " + e.getMessage());
            return null;
        }
    }

    // --- 3. MISE A JOUR DES MÉTHODES MÉTIER ---

    public Document create(Document document, String token) {
        // Remplacement de tokenService.validate par validateTokenViaActor
        Long userId = validateTokenViaActor(token);

        if (userId == null) { 
            throw new RuntimeException("Utilisateur non authentifié (Token invalide ou Timeout)"); 
        }

        String authorName = userRepository.findById(userId)
            .map(u -> u.getFirstName() + " " + u.getLastName())
            .orElse("Anonymous");

        document.setOwnerId(userId);
        document.setLastModifiedBy(userId);
        document.setAuthor(authorName);

        Document savedDoc = repo.save(document);

        DocumentAcces acc = new DocumentAcces();
        acc.setDocumentId(savedDoc.getId());
        acc.setUserId(userId);
        acc.setAccessType("owner");
        accesRepository.save(acc);

        return savedDoc;
    }

    public Document update(Long id, Document document, String token) {
        // Validation via Acteur
        Long userId = validateTokenViaActor(token);
        if (userId == null) throw new RuntimeException("Unauthorized");

        return repo.findById(id).map(existingDocument -> {
            existingDocument.setTitle(document.getTitle());
            existingDocument.setContent(document.getContent());
            existingDocument.setLastModifiedBy(userId);
            return repo.save(existingDocument);
        }).orElse(null);
    }

    public List<Document> getUserDocumentsFromToken(String token) {
        // Validation via Acteur
        Long userId = validateTokenViaActor(token);

        if (userId == null) {
             throw new RuntimeException("Token invalide ou expiré");
        }

        return getUserDocuments(userId);
    }

    public List<Document> getUserDocuments(Long userId) {
        List<DocumentAcces> accesList = accesRepository.findByUserId(userId);
        List<Long> documentIds = accesList.stream().map(DocumentAcces::getDocumentId).collect(Collectors.toList());
        return repo.findAllById(documentIds);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
    
    public Optional<Document> getByIdDirect(Long id) {
        return repo.findById(id);
    }
}