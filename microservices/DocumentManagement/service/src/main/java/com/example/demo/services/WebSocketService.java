package com.example.demo.services;

import main.java.com.cyFramework.core.Acteur;
import main.java.com.cyFramework.core.Message;

/**
 * Service de gestion des communications WebSocket basé sur le modèle d'Acteurs.
 * Assure la médiation entre les flux de données temps-réel et la logique métier.
 * Ce composant gère le cycle de vie des messages asynchrones du frontend.
 */
public class WebSocketService extends Acteur {

    public WebSocketService() {
        // Initialisation de l'acteur avec l'identifiant unique pour le dispatcher
        super("WebSocketService");
        this.demarrer();
    }

    /**
     * Point d'entrée principal (Overriding) pour la réception de messages.
     * @param message Enveloppe contenant l'émetteur et le payload.
     */
    @Override
    public void recevoirMessage(Message message) {

        // Logging systématique pour l'audit trail et la traçabilité des flux
        this.getLogger().info(
            "Message reçu | emetteur=" + message.getEmetteur() +
            " | contenu=" + message.getContenu()
        );

        // Routage sélectif des actions basées sur le protocole applicatif
        switch (message.getContenu()) {

            case "PING" -> {
                this.getLogger().info("Health check : PING reçu → OK");
            }

            case "SAVECONFIRM" -> {
                // Confirmation asynchrone de la couche de persistance
                this.getLogger().info("Persistence confirmée : File saved !");
            }

            case "handleSocket" -> {
                // Déclenchement manuel du handler pour les tests d'intégration
                this.getLogger().info("HandlingSocket - Interruption système détectée");
                this.handleSocket(0L, "user", "Internal Signal Trigger");
            }

            default -> {
                // Fallback pour les commandes non identifiées (Forward compatibility)
                this.getLogger().warn("Action inconnue ou non implémentée : " + message.getContenu());
            }
        }
    }

    /**
     * Formate et log les métadonnées d'un document pour le monitoring centralisé.
     */
    public void logDocId(Long docId, String sender, String content) {
        this.getLogger().info("Trace Flux Document [" + docId + "] : " + sender + " -> " + content);
    }

    /**
     * Dispatching d'une commande de sauvegarde vers le DocumentService.
     * Illustre le découplage via le passage de messages du framework.
     */
    public void sendSaveMessage(Long id, String document) {
        Message msg = new Message("WebSocketService", "DocumentService", id + "|" + document);
        //TODO: this.envoyerMessage("DocumentService", msg);
    }

    /**
     * Méthode de traitement pivot pour les flux sockets.
     * Centralise la logique de réception avant dispatching métier.
     */
    public void handleSocket(Long value, String user, String content) {
        // Redirection vers le logger de monitoring pour analyse télémétrique
        logDocId(value, user, content);
    }
}