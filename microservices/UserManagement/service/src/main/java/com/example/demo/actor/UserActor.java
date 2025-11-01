package com.example.demo.actor;

import com.example.demo.model.Role;

public class UserActor implements Actor {
    private final String id;
    private final String email;
    private final Role role;

    public UserActor(String id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    @Override
    public void receiveMessage(String message) {
        System.out.println("[UserActor " + id + "] Message reçu : " + message);
    }

    public Role getRole() {
        return role;
    }
}
