package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;


    public boolean sendResetToken(String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.out.println("[PasswordResetService] Utilisateur introuvable : " + email);
            return false;
        }

        String token = tokenService.createResetToken(user);

        String resetLink = "https://l'adresse_du_front/reset-password?token=" + token;

        emailService.send(
            email,
            "Réinitialisation de votre mot de passe",
            "Bonjour,\n\n" +
            "Voici votre lien pour réinitialiser votre mot de passe :\n" +
            resetLink + "\n\n" +
            "Si vous n’êtes pas à l’origine de cette demande, ignorez cet e-mail."
        );

        System.out.println("[PasswordResetService] Token envoyé à " + email);
        return true;
    }

    public boolean resetPassword(String tokenValue, String newPassword) {

        Long userId = tokenService.validate(tokenValue, "RESET");
        if (userId == null) {
            System.out.println("[PasswordResetService] Token invalide ou expiré : " + tokenValue);
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("[PasswordResetService] Aucun utilisateur pour ce token");
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenService.invalidate(tokenValue);

        System.out.println("[PasswordResetService] Mot de passe réinitialisé pour : " + user.getEmail());
        return true;
    }
}
