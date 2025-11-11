package com.example.demo.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Token;
import com.example.demo.repository.TokenRepository;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    private static final long LOGIN_TTL = 3600;
    private static final long RESET_TTL = 900;  // 15 min en gros

    public String createToken(Long userId, String type) {
        long ttl = type.equalsIgnoreCase("RESET") ? RESET_TTL : LOGIN_TTL;
        String value = UUID.randomUUID().toString();
        Token token = new Token(userId, value, type, Instant.now().plusSeconds(ttl));
        tokenRepository.save(token);
        return value;
    }

    public Long validate(String tokenValue, String type) {
        return tokenRepository.findByToken(tokenValue)
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .filter(t -> Instant.now().isBefore(t.getExpiresAt()))
                .map(Token::getUserId)
                .orElse(null);
    }

    public void invalidate(String tokenValue) {
        tokenRepository.deleteByToken(tokenValue);
    }

    public void cleanExpiredTokens() {
        tokenRepository.findAll().forEach(t -> {
            if (Instant.now().isAfter(t.getExpiresAt())) {
                tokenRepository.delete(t);
            }
        });
    }
}
