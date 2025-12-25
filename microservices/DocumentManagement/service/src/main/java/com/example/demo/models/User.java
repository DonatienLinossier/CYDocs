package com.example.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "User") // Assurez-vous que c'est le nom de la table dans MySQL
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    public User() {}

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
}