package com.example.demo.controllers;

import com.example.demo.models.Document;
import com.example.demo.services.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        String token = authHeader.replace("Bearer ", "");
        // On lance la validation asynchrone via Younes
        service.verifierTokenEtAction(token, document);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Création en cours (attente UserService)...");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        String token = authHeader.replace("Bearer ", "");
        document.setId(id);
        service.verifierTokenEtAction(token, document);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Mise à jour en cours...");
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        // Lecture directe pour le front
        return service.getByIdDirect(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserDocuments(userId));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        // On demande la validation avant suppression
        service.envoyerDemandeValidation(token); 
        // Note: La suppression réelle devra se faire dans traiterMessage
        return ResponseEntity.noContent().build();
    }
}