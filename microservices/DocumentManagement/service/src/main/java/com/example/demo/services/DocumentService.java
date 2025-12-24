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
public class DocumentService extends Acteur {

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    private final TokenService tokenService; // Injection ici

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository, TokenService tokenService) {
        super("DocumentManagementService");
        this.demarrer();
        this.repo = repo;
        this.accesRepository = accesRepository;
        this.tokenService = tokenService;
    }

    @Override
    public void recevoirMessage(Message message) {
        String contenuBrut = message.getContenu();
        if (contenuBrut == null) return;

        getLogger().info("Message reçu | emetteur=" + message.getEmetteur() + " | contenu=" + contenuBrut);
        String action = contenuBrut.contains(":") ? contenuBrut.split(":")[0] : contenuBrut;

        switch (action) {
            case "PING" -> {
                getLogger().info("PING reçu → OK");
            }
            default -> getLogger().warn("Action inconnue : " + action);
        }
    }

    public Document create(Document document, String token) {
        // Validation synchrone : on obtient l'ID tout de suite
        Long userId = tokenService.validate(token, "LOGIN");

        if (userId == null) {
            throw new RuntimeException("Accès refusé : Token invalide");
        }

        document.setOwnerId(userId);
        document.setLastModifiedBy(userId);
        Document savedDoc = repo.save(document); // Sauvegarde immédiate

        // Création de l'accès propriétaire
        DocumentAcces acc = new DocumentAcces();
        acc.setDocumentId(savedDoc.getId());
        acc.setUserId(userId);
        acc.setAccessType("owner");
        accesRepository.save(acc);

        return savedDoc; // On renvoie le document complet
    }

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