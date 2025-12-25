package com.example.demo.repositories;

import com.example.demo.models.DocumentAcces;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentAccesRepository extends JpaRepository<DocumentAcces, Long> {

    List<DocumentAcces> findByDocumentId(Long documentId);

    List<DocumentAcces> findByUserId(Long userId);

    Optional<DocumentAcces> findByDocumentIdAndUserId(Long documentId, Long userId);

}
