package main.java.com.cyFramework.core;

import java.util.UUID;

public abstract class Acteur {

    private final String id;
    private final String nom;
    private final Logger logger;
    private boolean actif = false;

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

