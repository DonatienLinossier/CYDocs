package com.example.demo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "User") // Assurez-vous que c'est le nom de la table dans MySQL
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String username;
    private String password;
    private String firstName; // AJOUT
    private String lastName;  // AJOUT

    public User() {}

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; } // AJOUT
    public String getLastName() { return lastName; }   // AJOUT
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; } // AJOUT
    public void setLastName(String lastName) { this.lastName = lastName; }
}