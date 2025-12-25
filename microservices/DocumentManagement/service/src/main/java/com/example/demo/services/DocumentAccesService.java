package com.example.demo.services;

import com.example.demo.models.DocumentAcces;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.stereotype.Service;
import com.example.demo.models.User;
@Service
public class DocumentAccesService {

    private final DocumentAccesRepository accesRepository;
    private final UserRepository userRepository;

    public DocumentAccesService(DocumentAccesRepository accesRepository, UserRepository userRepository) {
        this.accesRepository = accesRepository;
        this.userRepository = userRepository;
    }

    public void shareByEmail(Long documentId, String email) {
        // 1. Trouver l'ID de l'utilisateur cible via son mail
        userRepository.findByEmail(email).ifPresent(targetUser -> {
            
            // 2. Vérifier si l'accès existe déjà pour éviter les doublons
            boolean alreadyHasAccess = accesRepository.findByUserId(targetUser.getId())
                .stream()
                .anyMatch(acc -> acc.getDocumentId().equals(documentId));

            if (!alreadyHasAccess) {
                DocumentAcces newAccess = new DocumentAcces();
                newAccess.setDocumentId(documentId);
                newAccess.setUserId(targetUser.getId());
                newAccess.setAccessType("write"); // Par défaut en écriture
                accesRepository.save(newAccess);
            }
        });
    }
}