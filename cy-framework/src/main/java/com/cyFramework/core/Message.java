package com.cyFramework.core;

import java.time.Instant;

public class Message {
    private String emetteur;
    private String destinataire;
    private String contenu;
    private Instant horodatage = Instant.now();

    public Message() {}

    public Message(String emetteur, String destinataire, String contenu) {
        this.emetteur = emetteur;
        this.destinataire = destinataire;
        this.contenu = contenu;
    }

    public String getEmetteur() { return emetteur; }
    public String getDestinataire() { return destinataire; }
    public String getContenu() { return contenu; }
    public Instant getHorodatage() { return horodatage; }

    @Override
    public String toString() {
        return "[" + horodatage + "] " + emetteur + " → " + destinataire + " : " + contenu;
    }
}
