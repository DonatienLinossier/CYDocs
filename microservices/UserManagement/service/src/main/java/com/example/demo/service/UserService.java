package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import main.java.com.cyFramework.core.Acteur;
import main.java.com.cyFramework.core.Message;

@Service
public class UserService extends Acteur {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super("UserService");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.demarrer(); 
    }

    @Override
    public void recevoirMessage(Message message) {
        getLogger().info("Message reçu : " + message.getContenu());
    }

    public String login(String email, String rawPassword) {

        User existing = userRepository.findByEmail(email).orElse(null);

        if (existing != null && passwordEncoder.matches(rawPassword, existing.getPassword())) {

            String token = tokenService.createLoginToken(existing);
        
            getLogger().info("Connexion réussie : token signé = " + token);

            return token;
        }

        throw new RuntimeException("Email ou mot de passe incorrect !");
    }

    public void logout(String token) {

        Long userId = tokenService.validate(token, "LOGIN");
        if (userId == null) {
            throw new RuntimeException("Token invalide ou expiré — impossible de se déconnecter.");
        }

        tokenService.invalidate(token);


        getLogger().info("Déconnexion : token supprimé " + token);
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);

        getLogger().info("Utilisateur enregistré : " + saved.getUsername());

        return saved;
    }

    public boolean deleteUser(Long id) {

        User existing = userRepository.findById(id).orElse(null);

        if (existing == null) {
            getLogger().warn("Suppression échouée : utilisateur introuvable -> " + id);
            return false;
        }

        userRepository.delete(existing);

        getLogger().info("Utilisateur supprimé : " + existing.getUsername());

        return true;
    }

    private String fallbackAuth(String email, String rawPassword, Throwable t) {
        getLogger().error("Échec du service login (fallback) : " + t.getMessage());
        return "Service indisponible....";
    }

    public void updatePassword(Long id, String newRawPassword) {

        User u = userRepository.findById(id).orElse(null);
        if (u == null) return;

        u.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(u);

        getLogger().info("Mot de passe mis à jour pour l'utilisateur ID=" + id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
