package com.example.demo.repositories;

import com.example.demo.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// Ici les méthodes spécifiques au Document ; recherche par ownerId, etc.
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerId(Long ownerId);
}
