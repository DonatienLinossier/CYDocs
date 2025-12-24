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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class DocumentService extends Acteur {

    private final DocumentRepository repo;
    private final DocumentAccesRepository accesRepository;
    
    // Map pour stocker le document associé à un token en attendant la réponse de Younes
    private final Map<String, Document> attenteValidation = new ConcurrentHashMap<>();

    public DocumentService(DocumentRepository repo, DocumentAccesRepository accesRepository) {
        super("DocumentService");
        this.demarrer();
        this.repo = repo;
        this.accesRepository = accesRepository;
    }

    // Utilisé par le GET du controller pour un affichage immédiat
    public Optional<Document> getByIdDirect(Long id) {
        return repo.findById(id);
    }

    public void verifierTokenEtAction(String token, Document doc) {
        attenteValidation.put(token, doc);
        String contenu = "TOKEN_REQUEST:" + token;
        Message msg = new Message("DocumentService", "UserService", contenu);
        this.envoyerMessage("UserService", msg);
    }

    public void envoyerDemandeValidation(String token) {
        Message msg = new Message("DocumentService", "UserService", "TOKEN_REQUEST:" + token);
        this.envoyerMessage("UserService", msg);
    }

    @Override
    public void traiterMessage(Message message) {
        String contenu = message.getContenu();

        if (contenu.startsWith("TOKEN_SUCCESS:")) {
            try {
                Long userId = Long.parseLong(contenu.split(":")[1].trim());
                
                // On récupère le document en attente (Logique simplifiée : on prend le premier trouvé)
                // Dans un système réel, Younes devrait renvoyer le token dans sa réponse pour être précis
                if (!attenteValidation.isEmpty()) {
                    String tokenKey = attenteValidation.keySet().iterator().next();
                    Document doc = attenteValidation.remove(tokenKey);
                    
                    doc.setOwnerId(userId);
                    doc.setLastModifiedBy(userId);
                    repo.save(doc);
                    
                    // On crée l'accès si c'est un nouveau doc
                    if (accesRepository.findByUserId(userId).stream().noneMatch(a -> a.getDocumentId().equals(doc.getId()))) {
                        DocumentAcces acc = new DocumentAcces();
                        acc.setDocumentId(doc.getId());
                        acc.setUserId(userId);
                        acc.setAccessType("owner");
                        accesRepository.save(acc);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement du retour UserService: " + e.getMessage());
            }
        }
    }

    public List<Document> getUserDocuments(Long userId) {
        List<DocumentAcces> accesList = accesRepository.findByUserId(userId);
        List<Long> ids = accesList.stream().map(DocumentAcces::getDocumentId).collect(Collectors.toList());
        return repo.findAllById(ids);
    }
}