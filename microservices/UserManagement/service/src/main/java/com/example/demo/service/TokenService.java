package com.example.demo.service;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Token;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    private static final String SECRET = "super_secret_key_256_bits_minimum_OMG"; 
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final long LOGIN_TTL = 3600;    // 1h
    private static final long RESET_TTL = 900;     // 15 min

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(data.getBytes()));
    }


    public String createLoginToken(User user) {
        return createToken(user.getId(), user.getEmail(), "LOGIN", LOGIN_TTL);
    }

    public String createResetToken(User user) {
        return createToken(user.getId(), user.getEmail(), "RESET", RESET_TTL);
    }

    public String createToken(Long userId, String email, String type, long ttl) {
        try {
            // Payload JSON
            var payload = Map.of(
                "userId", userId,
                "email", email,
                "type", type,
                "exp", Instant.now().getEpochSecond() + ttl
            );

            String json = mapper.writeValueAsString(payload);

            String base64 = Base64.getUrlEncoder().withoutPadding()
                                  .encodeToString(json.getBytes());

            String signature = sign(base64);

            String token = base64 + "." + signature;

            tokenRepository.save(new Token(userId, token, type, Instant.now().plusSeconds(ttl)));

            return token;

        } catch (Exception e) {
            throw new RuntimeException("Erreur création token : " + e.getMessage(), e);
        }
    }

    public Long validate(String token, String expectedType) {
        try {
            var dbToken = tokenRepository.findByToken(token);
            if (dbToken.isEmpty()) return null;

            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;

            String payload64 = parts[0];
            String signature = parts[1];

            if (!sign(payload64).equals(signature)) return null;

   
            String json = new String(Base64.getUrlDecoder().decode(payload64));
            Map<String, Object> payload = mapper.readValue(json, Map.class);

            if (!payload.get("type").equals(expectedType)) return null;

            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) return null;

            return ((Number) payload.get("userId")).longValue();

        } catch (Exception e) {
            return null;
        }
    }

    public void invalidate(String token) {
        tokenRepository.deleteByToken(token);
    }
}
