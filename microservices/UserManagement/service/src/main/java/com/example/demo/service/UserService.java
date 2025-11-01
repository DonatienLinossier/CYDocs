package com.example.demo.service;

import com.example.demo.actor.ActorContainer;
import com.example.demo.actor.UserActor;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ActorContainer actorContainer;

    public UserService(UserRepository userRepository, ActorContainer actorContainer) {
        this.userRepository = userRepository;
        this.actorContainer = actorContainer;
    }

    public User registerUser(User user) {
        User saved = userRepository.save(user);
        actorContainer.registerActor(saved.getUsername(),
                new UserActor(saved.getUsername(), saved.getUsername(), saved.getRole()));
        System.out.println("[UserService] Utilisateur enregistré en base : " + saved.getUsername());
        return saved;
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackAuth")
    public String login(User user) {
        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null && existing.getPassword().equals(user.getPassword())) {
            return "JWT_TOKEN_EXAMPLE_" + user.getUsername();
        }
        throw new RuntimeException("Invalid credentials");
    }

    private String fallbackAuth(User user, Throwable t) {
        return "Service unavailable, please retry later";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
