package com.example.demo.services;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository) {
        this.repo = repo;
        this.accesRepository = accesRepository;
    }

    public Optional<Document> getById(Long id) {
        return repo.findById(id);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
    public Document create(Document document) {
        return repo.save(document);
    }
    public Document update(Long id, Document document) {
        return repo.findById(id).map(existingDocument -> {
            existingDocument.setTitle(document.getTitle());
            existingDocument.setContent(document.getContent());
            existingDocument.setLastModifiedBy(document.getLastModifiedBy());
            return repo.save(existingDocument);
        }).orElse(null);
    }
    


    public List<Document> getUserDocuments(Long userId) {
        // Récupère tous les documents auxquels l'utilisateur a accès
        List<DocumentAcces> accesList = accesRepository.findByUserId(userId);

        // On prends que les ID des documents de la liste d'accès
        List<Long> documentIds = accesList.stream().map(DocumentAcces::getDocumentId).collect(Collectors.toList());

        // Récupère tous les documents correspondants
        return repo.findAllById(documentIds);
    }
}