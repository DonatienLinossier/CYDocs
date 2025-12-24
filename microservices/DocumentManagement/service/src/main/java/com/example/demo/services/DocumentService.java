package com.example.demo.services;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.example.demo.models.Message;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentService extends Acteur { // Hérite du framework d'Ilan

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    
    // Stockage temporaire pour les créations en cours
    private final Map<String, Document> documentsEnAttente = new ConcurrentHashMap<>();

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository) {
        super("DocumentManagementService"); // Nom de l'acteur
        this.demarrer(); // Lance le thread de l'acteur
        this.repo = repo;
        this.accesRepository = accesRepository;
    }

    // --- 1. LOGIQUE DE CRÉATION (ASYNCHRONE AVEC YOUNES) ---

    public void create(Document document, String token) {
        documentsEnAttente.put(token, document);
        demanderUserId(token); // Envoie le message à Younes
    }

    public void demanderUserId(String token) {
        String contenu = "TOKEN_REQUEST:" + token;
        Message msg = new Message("DocumentManagementService", "UserService", contenu);
        this.envoyerMessage("UserService", msg); // Utilise le framework
    }

    @Override
    public void traiterMessage(Message message) {
        String contenu = message.getContenu();
        if (contenu.startsWith("TOKEN_SUCCESS:")) {
            Long userId = Long.parseLong(contenu.split(":")[1].trim());
            
            // On récupère le document correspondant au token
            // Note: Idéalement le message devrait contenir le token pour être précis
            for (String token : documentsEnAttente.keySet()) {
                Document doc = documentsEnAttente.remove(token);
                doc.setOwnerId(userId); //
                doc.setLastModifiedBy(userId); //
                Document savedDoc = repo.save(doc); //

                // Création automatique de l'accès pour le propriétaire
                DocumentAcces access = new DocumentAcces();
                access.setDocumentId(savedDoc.getId());
                access.setUserId(userId);
                access.setAccessType("owner");
                accesRepository.save(access); //
                break;
            }
        }
    }

    // --- 2. LOGIQUE DE GESTION CLASSIQUE (SYNCHRONE) ---

    public Optional<Document> getByIdDirect(Long id) {
        return repo.findById(id); //
    }

    public Document update(Long id, Document document, String token) {
        // Optionnel: On pourrait aussi demander l'ID à Younes ici pour vérifier l'accès
        return repo.findById(id).map(existingDocument -> {
            existingDocument.setTitle(document.getTitle());
            existingDocument.setContent(document.getContent());
            // Mise à jour simplifiée (on pourrait raffiner avec l'ID de Younes)
            return repo.save(existingDocument); //
        }).orElse(null);
    }

    public List<Document> getUserDocuments(Long userId) {
        // Récupère les IDs de documents via la table d'accès
        List<DocumentAcces> accesList = accesRepository.findByUserId(userId);
        List<Long> documentIds = accesList.stream().map(DocumentAcces::getDocumentId).collect(Collectors.toList());

        return repo.findAllById(documentIds); //
    }

    public void delete(Long id) {
        repo.deleteById(id); //
    }
}