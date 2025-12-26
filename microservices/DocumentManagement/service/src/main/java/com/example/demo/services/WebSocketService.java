package com.example.demo.services;

import com.cyFramework.core.Acteur;
import com.cyFramework.core.Message;

/**
 * Service de gestion des communications WebSocket basé sur le modèle d'Acteurs.
 * Gestion des flux de données temps-réel
 * Gère le cycle de vie des messages asynchrones du frontend.
 */
public class WebSocketService extends Acteur {

    public WebSocketService() {
        // Initialisation de l'acteur
        super("WebSocketService");
        this.demarrer();
    }

    /**
     * Point d'entrée principal pour la réception de messages.
     * @param message Enveloppe contenant l'émetteur et le payload.
     */
    @Override
    public void recevoirMessage(Message message) {

        // Logging
        this.getLogger().info(
            "Message reçu | emetteur=" + message.getEmetteur() +
            " | contenu=" + message.getContenu()
        );

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
                // Fallback pour les commandes non identifiées
                this.getLogger().warn("Action inconnue ou non implémentée : " + message.getContenu());
            }
        }
    }

    /**
     * Formate et log les métadonnées d'un document
     */
    public void logDocId(Long docId, String sender, String content) {
        this.getLogger().info("Trace Flux Document [" + docId + "] : " + sender + " -> " + content);
    }

    /**
     * Dispatching d'une commande de sauvegarde vers le DocumentService..
     */
    public void sendSaveMessage(Long id, String document) {
        Message msg = new Message("WebSocketService", "DocumentService", id + "|" + document);
    }

    /**
     * Méthode de traitement pour les flux sockets.
     */
    public void handleSocket(Long value, String user, String content) {
        // Redirection vers le logger de monitoring pour analyse télémétrique
        logDocId(value, user, content);
    }
}
