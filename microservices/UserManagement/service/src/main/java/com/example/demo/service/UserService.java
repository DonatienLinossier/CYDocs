package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cyFramework.core.Acteur;
import com.cyFramework.core.Message;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;


@Service
public class UserService extends Acteur {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super("user-management");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.demarrer(); 

        //this.recevoirMessage(new Message("DocumentManagementService", "UserService", "PING"));

    }

    
    @Override
    public void recevoirMessage(Message message) {
        String contenuBrut = message.getContenu();
        if (contenuBrut == null) return;

        getLogger().info("Message reçu | emetteur=" + message.getEmetteur() + " | contenu=" + contenuBrut);
        String action = contenuBrut.contains(":") ? contenuBrut.split(":")[0] : contenuBrut;

        switch (action) {
            case "PING" -> {
                getLogger().info("PING reçu → OK");
            }

            case "TOKEN_REQUEST" -> {
                try {
                    String token = contenuBrut.substring(contenuBrut.indexOf(":") + 1).trim();
                    
                    Long userId = getUserIdFromToken(token);
                    String contenuReponse = "TOKEN_VALIDATED:" + token + ":" + userId;
                    Message reponse = new Message("UserService", message.getEmetteur(), contenuReponse);
                    this.envoyerMessage(message.getEmetteur(), reponse);
                    
                    getLogger().info("Validation réussie. Réponse envoyée : " + contenuReponse);
                    
                } catch (Exception e) {
                    getLogger().error("Token invalide ou erreur : " + e.getMessage());
                    String tokenRate = contenuBrut.contains(":") ? contenuBrut.substring(contenuBrut.indexOf(":") + 1).trim() : "unknown";
                    
                    this.envoyerMessage(message.getEmetteur(), 
                        new Message("UserService", message.getEmetteur(), "TOKEN_INVALID:" + tokenRate));
                }
            }

            default -> getLogger().warn("Action inconnue : " + action);
        }
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

    public Long getUserIdFromToken(String token) {

        Long userId = tokenService.validate(token, "LOGIN");

        if (userId == null) {
            getLogger().warn("Token invalide ou expiré — impossible de récupérer l'utilisateur.");
            throw new RuntimeException("Token invalide ou expiré.");
        }

        getLogger().info("ID utilisateur récupéré via token : " + userId);
        return userId;
    }


    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
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
