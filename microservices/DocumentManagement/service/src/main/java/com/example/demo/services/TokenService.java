package com.example.demo.services;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {


    @Value("${jwt.secret:super_secret_key_256_bits_minimum_OMG_123456}")
    private String secretKey;

    public Long validate(String token, String type) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // On vérifie si c'est bien un token de type LOGIN
            if (!type.equals(claims.get("type"))) {
                return null;
            }

            // On récupère l'ID (Younes doit l'avoir mis dans le "subject" ou un claim "userId")
            return Long.parseLong(claims.getSubject()); 
        } catch (Exception e) {
            return null; // Token invalide, expiré ou mal signé
        }
    }
}