package com.example.demo.services;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    private final TokenService tokenService; // Injection ici

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository, TokenService tokenService) {
        this.repo = repo;
        this.accesRepository = accesRepository;
        this.tokenService = tokenService;
    }


    public Document create(Document document, String token) {
    // FORCE UN ID POUR TESTER LE RESTE
    Long userId = 1L; 

    /* Commente ça temporairement
    Long userId = tokenService.validate(token, "LOGIN");
    if (userId == null) { throw new RuntimeException("..."); }
    */

    document.setOwnerId(userId);
    document.setLastModifiedBy(userId);
    Document savedDoc = repo.save(document);

    DocumentAcces acc = new DocumentAcces();
    acc.setDocumentId(savedDoc.getId());
    acc.setUserId(userId);
    acc.setAccessType("owner");
    accesRepository.save(acc);

    return savedDoc;
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
    // Dans DocumentService.java

public List<Document> getUserDocumentsFromToken(String token) {
    // 1. Valider le token et récupérer l'ID de l'utilisateur
    // On réutilise la logique que vous aviez commentée
    Long userId = tokenService.validate(token, "LOGIN"); 

    if (userId == null) {
        // Optionnel : Forcer l'ID 1L pour vos tests actuels si le TokenService n'est pas fini
        userId = 1L; 
        // Sinon : throw new RuntimeException("Token invalide ou expiré");
    }

    // 2. Récupérer les documents liés à cet ID via la table d'accès
    return getUserDocuments(userId);
}
}