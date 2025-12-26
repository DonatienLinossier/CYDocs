package com.example.demo.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.Document;
import com.example.demo.services.DocumentService;
import com.example.demo.services.DocumentAccesService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;
    private final DocumentAccesService accessService;
    // REMOVED: private final TokenService tokenService; 

    public DocumentController(DocumentService service, 
                              DocumentAccesService accessService) {
        this.service = service;
        this.accessService = accessService;
        // REMOVED: this.tokenService = tokenService;
    }


    // --- ACCESS MANAGEMENT METHODS ---

    @PostMapping("/share")
    public ResponseEntity<?> share(@RequestHeader("Authorization") String authHeader, @RequestBody ShareRequest request) {
        String token = authHeader.replace("Bearer ", "");
        
        // CORRECTION: Use service.validateTokenViaActor instead of tokenService.validate
        Long requesterId = service.validateTokenViaActor(token); 
        
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        accessService.shareByEmail(request.getDocumentId(), request.getTargetEmail(), request.getAccessType(), requesterId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{docId}/collaborators")
    public ResponseEntity<List<DocumentAccesService.CollaboratorDTO>> getCollaborators(@PathVariable Long docId) {
        return ResponseEntity.ok(accessService.getCollaborators(docId));
    }

    @DeleteMapping("/access/{docId}/{userId}")
    public ResponseEntity<?> revoke(@RequestHeader("Authorization") String authHeader, 
                                    @PathVariable Long docId, 
                                    @PathVariable Long userId) {
        String token = authHeader.replace("Bearer ", "");
        
        // CORRECTION: Use service.validateTokenViaActor
        Long requesterId = service.validateTokenViaActor(token);
        
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        accessService.revokeAccess(docId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // --- DOCUMENT MANAGEMENT METHODS ---

    @GetMapping("/my-documents")
    public ResponseEntity<List<Document>> getMyDocuments(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        // This method already uses the actor internally in DocumentService
        List<Document> docs = service.getUserDocumentsFromToken(token);
        return ResponseEntity.ok(docs);
    }

    @PostMapping("/create")
    public ResponseEntity<Document> create(@RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        String token = authHeader.replace("Bearer ", "");
        try {
            // DocumentService.create already handles validation internally via Actor
            Document created = service.create(document, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(created); 
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id, @RequestHeader("Authorization") String authHeader, @RequestBody Document document) {
        String token = authHeader.replace("Bearer ", "");
        if (service.getByIdDirect(id).isEmpty()) return ResponseEntity.notFound().build();

        try {
            // DocumentService.update already handles validation internally via Actor
            Document updated = service.update(id, document, token);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Document> getById(
        @PathVariable Long id, 
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        
        // CORRECTION: Use service.validateTokenViaActor
        Long userId = service.validateTokenViaActor(token);
        
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return service.getByIdDirect(id).map(doc -> {
            // 1. If user is owner
            if (doc.getOwnerId().equals(userId)) {
                doc.setCurrentPermission("write");
            } else {
                // 2. If user is collaborator
                String permission = accessService.getUserPermission(id, userId); 
                doc.setCurrentPermission(permission);
            }

            // 3. Security: No rights found
            if (doc.getCurrentPermission() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Document>build();
            }

            return ResponseEntity.ok(doc);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getByIdDirect(id).isEmpty()) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public static class ShareRequest {
        private Long documentId;
        private String targetEmail;
        private String accessType; 

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getTargetEmail() { return targetEmail; }
        public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
        public String getAccessType() { return accessType; }
        public void setAccessType(String accessType) { this.accessType = accessType; }
    }
}