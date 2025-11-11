package com.example.demo.repositories;

import com.example.demo.models.DocumentAcces;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// Ici les méthodes spécifiques au Document ; recherche par ownerId, etc.
public interface DocumentAccesRepository extends JpaRepository<DocumentAcces, Long> {

    List<DocumentAcces> findByDocumentId(Long documentId);

    List<DocumentAcces> findByUserId(Long userId);
}