package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class DocumentAcces{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;
    private Long userId;
    private String accessType; // "read", "write", "owner"

    public DocumentAcces() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getAccessType() {
        return accessType;
    }
    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }
}