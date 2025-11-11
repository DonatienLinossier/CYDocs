package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private LocalDateTime lastModified; // date de la dernière modification
    private Long lastModifiedBy; // dernier utilisateur ayant modifié le doc

    private Long ownerId; // l’utilisateur qui a créé le doc

    public Document() {}

    @PrePersist @PreUpdate
    public void updateTimestamp() {
        this.lastModified = LocalDateTime.now();
    }
    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    public Long getLastModifiedBy() {
        return lastModifiedBy;
    }
    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    public Long getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

}
