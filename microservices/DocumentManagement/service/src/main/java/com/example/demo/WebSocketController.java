package com.example.demo;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // DTO for messages
    public static class DocMessage {
        private String sender;
        private String content;

        public DocMessage() {}

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Client sends updates here: /app/doc/{docId}
    @MessageMapping("/doc/{docId}")
    @SendTo("/topic/doc/{docId}") // Broadcast to all users on this doc
    public DocMessage updateDoc(@DestinationVariable String docId, DocMessage message) {
        System.out.println("Received on doc " + docId + ": " + message.getSender() + " -> " + message.getContent()); //TODO: use framework log

        // TODO: check permission, handle conflicts, save to db.

        return message; // Broadcast to all subscribers
    }
}
