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
    @PostMapping("/create")
    public Document create(@RequestBody Document document) {
        return service.create(document);
    }

    // Méthode UPDATE
    @PutMapping("/update/{id}")
    public Document update(@PathVariable Long id, @RequestBody Document document) {
        return service.update(id, document);
    }

    // Liste des documents de l’utilisateur (UserSpace)
    @GetMapping("/user/{userId}")
    public List<Document> getUserDocuments(@PathVariable Long userId) {
        return service.getUserDocuments(userId);
    }

    // Récupérer un document
    @GetMapping("get/{id}")
    public Document getById(@PathVariable Long id) {
        return service.getById(id).orElse(null);
    }

    // Supprimer
    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
