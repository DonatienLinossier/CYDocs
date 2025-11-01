package com.example.demo.actor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ActorContainer {
    private final Map<String, Actor> actors = new ConcurrentHashMap<>();

    public void registerActor(String id, Actor actor) {
        actors.put(id, actor);
    }

    public void sendMessage(String id, String message) {
        Actor actor = actors.get(id);
        if (actor != null) {
            actor.receiveMessage(message);
        } else {
            System.out.println("[ActorContainer] Aucun acteur trouvé avec id: " + id);
        }
    }
}
