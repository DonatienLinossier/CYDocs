package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tokens", indexes = {
    @Index(columnList = "token", unique = true)
})
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(unique = true, nullable = false, length = 500)
    private String token;

    private String type;

    private Instant expiresAt;

    public Token() {}

    public Token(Long userId, String token, String type, Instant expiresAt) {
        this.userId = userId;
        this.token = token;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public String getType() { return type; }
    public Instant getExpiresAt() { return expiresAt; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setToken(String token) { this.token = token; }
    public void setType(String type) { this.type = type; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
