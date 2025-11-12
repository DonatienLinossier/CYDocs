package com.example.demo.controllers;

import com.example.demo.models.Document;
import com.example.demo.services.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// Fonction pour gérer les requêtes/front controller
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // Méthode SAVE
    @PostMapping("/save")
    public Document save(@RequestBody Document document) {
        return service.save(document);
    }

    // Liste des documents de l’utilisateur (UserSpace)
    @GetMapping("/user/{userId}")
    public List<Document> getUserDocuments(@PathVariable Long userId) {
        return service.getUserDocuments(userId);
    }

    // Récupérer un document
    @GetMapping("/{id}")
    public Document getById(@PathVariable Long id) {
        return service.getById(id).orElse(null);
    }

    // Supprimer
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
