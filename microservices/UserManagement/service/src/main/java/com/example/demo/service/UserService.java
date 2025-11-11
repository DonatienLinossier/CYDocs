package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.service.TokenService;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackAuth")
    public String login(String email, String rawPassword) {
        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null && passwordEncoder.matches(rawPassword, existing.getPassword())) {
            String token = tokenService.createToken(existing.getId(), "LOGIN");
            System.out.println("[UserService] Connexion réussie : token créé " + token);
            return token;
        }
        throw new RuntimeException("Email ou mot de passe incorrect !");
    }


    public void logout(String token) {
        tokenService.invalidate(token);
        System.out.println("[UserService] Déconnexion : token supprimé " + token);
    }


    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        System.out.println("[UserService] Utilisateur enregistré en base : " + saved.getUsername());
        return saved;
    }

    public boolean deleteUser(Long id) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            System.out.println("[UserService] Utilisateur Inexistant: " + id);
            return false;
        }
        userRepository.delete(existing);
        System.out.println("Utilisateur supprimé : " + existing.getUsername());
        return true;
    }

    private String fallbackAuth(User user, Throwable t) {
        return "Service indisponible....";
    }

    public void updatePassword(Long id, String newRawPassword) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) return;
        u.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

     public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
