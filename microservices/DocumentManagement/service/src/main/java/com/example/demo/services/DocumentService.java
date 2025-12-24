package com.example.demo.services;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.cyFramework.core.Acteur;
import com.cyFramework.core.Message;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentService extends Acteur { // Hérite du framework d'Ilan

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    
    // Stockage temporaire pour les créations en cours
    private final Map<String, Document> documentsEnAttente = new ConcurrentHashMap<>();

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository) {
        super("DocumentManagementService"); // Nom de l'acteur
        this.demarrer(); // Lance le thread de l'acteur
        this.repo = repo;
        this.accesRepository = accesRepository;
    }

    // --- 1. LOGIQUE DE CRÉATION (ASYNCHRONE AVEC YOUNES) ---

    public void create(Document document, String token) {
        documentsEnAttente.put(token, document);
        demanderUserId(token); // Envoie le message à Younes
    }

    public void demanderUserId(String token) {
        String contenu = "TOKEN_REQUEST:" + token;
        Message msg = new Message("DocumentManagementService", "UserService", contenu);
        this.envoyerMessage("UserService", msg); // Utilise le framework
    }

    @Override
public void traiterMessage(Message message) {
    String contenu = message.getContenu();

    // Format attendu de Younes : "token:eyJhbGci...:123"
    if (contenu.startsWith("token:")) {
        try {
            String[] parts = contenu.split(":");
            
            if (parts.length >= 3) {
                String tokenRecu = parts[1].trim(); // Récupère le token
                Long userId = Long.parseLong(parts[2].trim()); // Récupère l'ID

                // On retrouve le BON document grâce au token
                Document doc = documentsEnAttente.remove(tokenRecu);

                if (doc != null) {
                    doc.setOwnerId(userId);
                    doc.setLastModifiedBy(userId);
                    Document savedDoc = repo.save(doc);
                    
                    // Création du droit d'accès pour que l'utilisateur puisse voir son doc
                    DocumentAcces acc = new DocumentAcces();
                    acc.setDocumentId(savedDoc.getId());
                    acc.setUserId(userId);
                    acc.setAccessType("owner");
                    accesRepository.save(acc);
                    
                    getLogger().info("Document créé avec succès pour l'utilisateur ID: " + userId);
                } else {
                    getLogger().warn("Validation reçue mais aucun document trouvé pour ce token.");
                }
            }
        } catch (Exception e) {
            getLogger().error("Erreur de parsing sur le message de Younes : " + e.getMessage());
        }
    }
}

    // --- 2. LOGIQUE DE GESTION CLASSIQUE (SYNCHRONE) ---

    public Optional<Document> getByIdDirect(Long id) {
        return repo.findById(id); //
    }

    public Document update(Long id, Document document, String token) {
        // Optionnel: On pourrait aussi demander l'ID à Younes ici pour vérifier l'accès
        return repo.findById(id).map(existingDocument -> {
            existingDocument.setTitle(document.getTitle());
            existingDocument.setContent(document.getContent());
            // Mise à jour simplifiée (on pourrait raffiner avec l'ID de Younes)
            return repo.save(existingDocument); //
        }).orElse(null);
    }

    public List<Document> getUserDocuments(Long userId) {
        // Récupère les IDs de documents via la table d'accès
        List<DocumentAcces> accesList = accesRepository.findByUserId(userId);
        List<Long> documentIds = accesList.stream().map(DocumentAcces::getDocumentId).collect(Collectors.toList());

        return repo.findAllById(documentIds); //
    }

    public void delete(Long id) {
        repo.deleteById(id); //
    }
}