package com.cyFramework.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public abstract class Acteur {

    private final String id;
    private final String nom;
    private final Logger logger;
    private boolean actif = false;
    private final Queue<Message> boiteMessages = new LinkedList<>();

    protected Acteur(String nom) {
        this.id = UUID.randomUUID().toString();
        this.nom = nom;
        this.logger = new Logger(nom);
    }

    public void demarrer() {
        actif = true;
        logger.info("Acteur démarré.");
        ActorRegistry.enregistrer(this);
    }

    public void arreter() {
        actif = false;
        logger.info("Acteur arrêté.");
        ActorRegistry.retirer(this.nom);
    }

    /** 
     * Méthode appelée quand un message est reçu.
     */
    public abstract void recevoirMessage(Message message);

    /**
     * Envoi d’un message. Le framework gère automatiquement
     * s’il s’agit d’un envoi local ou distant.
     */
    public void envoyerMessage(String serviceCible, Message message) {
        MessageSender.getInstance().envoyer(serviceCible, message);
    }

    /** fonctionnement interne : on stocke en FIFO les messages */
    public void ajouterMessage(Message msg) {
        synchronized (boiteMessages) {
            boiteMessages.add(msg);
        }
    }

    /** Appelé périodiquement : traite la file FIFO */
    public void tick() {
        Message msg = null;

        synchronized (boiteMessages) {
            if (!boiteMessages.isEmpty()) {
                msg = boiteMessages.poll();
            }
        }

        if (msg != null) {
            recevoirMessage(msg); // logique existante
        }
    }

    public String getNom() { return nom; }
    public boolean estActif() { return actif; }
    public Logger getLogger() { return logger; }
    public String getId() { return id; }

    @Override
    public String toString() {
        return "Acteur{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", actif=" + actif +
                '}';
    }
}

