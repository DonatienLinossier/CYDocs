package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Document;
import com.example.demo.services.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<Document> create(@RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        // 1. Extraire le token du header
        String token = authHeader.replace("Bearer ", "");

        // 2. Vérifier si l'ID existe déjà (protection contre doublons)
        if (document.getId() != null && service.getByIdDirect(document.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }

        // 3. Appeler le service avec le TOKEN
        Document created = service.create(document, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); 
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id, @RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        String token = authHeader.replace("Bearer ", "");
        
        if (service.getByIdDirect(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document updated = service.update(id, document, token);
        return ResponseEntity.ok(updated);
    }

    // GET DOCUMENTS FOR USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable Long userId) {
        // userId est passé ici directement pour la récupération des accès
        List<Document> docs = service.getUserDocuments(userId);
        return ResponseEntity.ok(docs);
    }

    // GET DOCUMENT BY ID
    @GetMapping("/get/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        // Lecture directe pour l'affichage immédiat dans le front
        return service.getByIdDirect(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getByIdDirect(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    // TEST : Accessible via GET /documents/test/
    @GetMapping("/test/")
    public ResponseEntity<String> test() {
        // 202 Accepted est parfait pour signaler que le serveur a bien reçu la requête
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Test validé...");
    }
    // Dans DocumentController.java

// Récupérer TOUS les documents de l'utilisateur connecté via son TOKEN
@GetMapping("/my-documents")
public ResponseEntity<List<Document>> getMyDocuments(@RequestHeader("Authorization") String authHeader) {
    // 1. Extraction du token
    String token = authHeader.replace("Bearer ", "");

    // 2. Appel du service qui va décoder le token pour trouver l'utilisateur
    List<Document> docs = service.getUserDocumentsFromToken(token);
    
    return ResponseEntity.ok(docs);
}
}