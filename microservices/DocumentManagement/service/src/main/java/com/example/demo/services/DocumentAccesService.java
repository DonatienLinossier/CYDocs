package com.example.demo.services;

import com.example.demo.models.DocumentAcces;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;
import com.example.demo.models.User;
import com.example.demo.repositories.DocumentRepository; 
import java.util.Optional;
import java.util.List;
@Service
public class DocumentAccesService {

    private final DocumentAccesRepository accesRepository;
    private final UserRepository userRepository;
private final DocumentRepository documentRepository; // À injecter
    public DocumentAccesService(DocumentAccesRepository accesRepository, UserRepository userRepository, DocumentRepository documentRepository) {
        this.accesRepository = accesRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
    }

    // 1. Partage avec contrôle du type (read/write)
    public void shareByEmail(Long documentId, String email, String type, Long requesterId) {
        // Vérification de sécurité : Seul l'owner peut partager
        documentRepository.findById(documentId).ifPresent(doc -> {
            if (!doc.getOwnerId().equals(requesterId)) {
                throw new RuntimeException("Seul le propriétaire peut partager ce document");
            }

            userRepository.findByEmail(email).ifPresent(targetUser -> {
                DocumentAcces acc = accesRepository.findByDocumentIdAndUserId(documentId, targetUser.getId())
                        .orElse(new DocumentAcces());
                
                acc.setDocumentId(documentId);
                acc.setUserId(targetUser.getId());
                acc.setAccessType(type); // "read" ou "write"
                accesRepository.save(acc);
            });
        });
    }
// Dans DocumentAccesService.java
public String getUserPermission(Long documentId, Long userId) {
    return accesRepository.findByDocumentIdAndUserId(documentId, userId)
            .map(acc -> acc.getAccessType()) // Renvoie "read" ou "write"
            .orElse(null); // Aucun accès trouvé
}
    // 2. Retirer l'accès
    public void revokeAccess(Long documentId, Long targetUserId, Long requesterId) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            if (doc.getOwnerId().equals(requesterId)) {
                accesRepository.findByDocumentIdAndUserId(documentId, targetUserId)
                        .ifPresent(accesRepository::delete);
            }
        });
    }
    // Ajoutez cette méthode pour récupérer les détails des collaborateurs
public List<CollaboratorDTO> getCollaborators(Long documentId) {
    return accesRepository.findByDocumentId(documentId).stream()
        .map(acc -> {
            User user = userRepository.findById(acc.getUserId()).orElse(null);
            return new CollaboratorDTO(
                acc.getUserId(),
                user != null ? user.getEmail() : "Inconnu",
                user != null ? (user.getFirstName() + " " + user.getLastName()) : "Inconnu",
                acc.getAccessType()
            );
        })
        .filter(c -> !c.accessType().equals("owner")) 
        .toList();
}

// Petit DTO pour envoyer les infos propres au Front
public record CollaboratorDTO(Long userId, String email, String name, String accessType) {}
}