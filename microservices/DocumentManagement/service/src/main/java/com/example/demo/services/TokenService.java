package com.example.demo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;
import java.time.Instant;

@Service
public class TokenService {

    // IMPORTANT : Doit être identique à la clé du UserService
    private static final String SECRET = "super_secret_key_256_bits_minimum_OMG"; 
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Recrée la signature HMAC-SHA256 pour comparer avec celle du token
     */
    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(data.getBytes()));
    }

    /**
     * Valide le token de manière autonome
     */
    public Long validate(String token, String expectedType) {
        try {
            // 1. Découpage du token (Format: PayloadBase64.Signature)
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;

            String payload64 = parts[0];
            String signatureProvided = parts[1];

            // 2. Vérification de l'intégrité (Signature)
            if (!sign(payload64).equals(signatureProvided)) {
                return null;
            }

            // 3. Décodage du JSON
            String json = new String(Base64.getUrlDecoder().decode(payload64));
            Map<String, Object> payload = mapper.readValue(json, Map.class);

            // 4. Vérification du type (LOGIN ou RESET)
            if (!payload.get("type").equals(expectedType)) {
                return null;
            }

            // 5. Vérification de l'expiration
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }

            // 6. Extraction de l'ID utilisateur
            return ((Number) payload.get("userId")).longValue();

        } catch (Exception e) {
            System.err.println("Erreur validation token: " + e.getMessage());
            return null;
        }
    }
}