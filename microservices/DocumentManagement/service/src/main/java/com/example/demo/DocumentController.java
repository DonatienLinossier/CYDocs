package com.example.demo.controllers;

import com.example.demo.models.Document;
import com.example.demo.services.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<Document> create(@RequestBody Document document) {
        if (document.getId() != null && service.getById(document.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }

        Document created = service.create(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // 201
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id, @RequestBody Document document) {

        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build(); // 404
        }

        Document updated = service.update(id, document);
        return ResponseEntity.ok(updated); // 200
    }

    // GET DOCUMENTS FOR USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Document>> getUserDocuments(@PathVariable Long userId) {
        List<Document> docs = service.getUserDocuments(userId);

        return ResponseEntity.ok(docs); // 200
    }

    // GET DOCUMENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build(); // 404
        }

        service.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
