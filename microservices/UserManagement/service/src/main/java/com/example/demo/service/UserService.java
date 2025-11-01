package com.example.demo.service;

import com.example.demo.actor.ActorContainer;
import com.example.demo.actor.UserActor;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    private final Map<String, User> users = new HashMap<>();
    private final ActorContainer actorContainer;

    public UserService(ActorContainer actorContainer) {
        this.actorContainer = actorContainer;
    }

    public User registerUser(User user) {
        users.put(user.getUsername(), user);
        actorContainer.registerActor(user.getUsername(), new UserActor(user.getUsername(), user.getUsername(), user.getRole()));
        System.out.println("[UserService] Utilisateur enregistré : " + user.getUsername());
        return user;
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackAuth")
    public String login(User user) {
        User existing = users.get(user.getUsername());
        if (existing != null && existing.getPassword().equals(user.getPassword())) {
            return "JWT_TOKEN_EXAMPLE_" + user.getUsername();
        }
        throw new RuntimeException("Invalid credentials");
    }

    private String fallbackAuth(User user, Throwable t) {
        return "Service unavailable, please retry later";
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
