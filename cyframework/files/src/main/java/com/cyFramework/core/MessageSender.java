package main.java.com.cyFramework.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessageSender {

    private static MessageSender instance;
    private final RestTemplate restTemplate;
    private final ConsulClientService consulClient;

    @Autowired
    public MessageSender(RestTemplateBuilder builder, ConsulClientService consulClient) {
        this.restTemplate = builder.build();
        this.consulClient = consulClient;
        instance = this;
    }

    public static MessageSender getInstance() {
        return instance;
    }

    /**
     * Envoie un message. Si l’acteur destinataire est local, 
     * on appelle directement sa méthode recevoirMessage().
     * Sinon, on passe par HTTP via Consul.
     */
    public void envoyer(String serviceCible, Message message) {
        // Vérifie si un acteur local existe
        Acteur acteurLocal = ActorRegistry.trouver(message.getDestinataire());

        if (acteurLocal != null) {
            acteurLocal.getLogger().debug("Message local reçu de " + message.getEmetteur());
            acteurLocal.recevoirMessage(message);
            return;
        }

        // Sinon : communication inter-service via Consul
        String url = consulClient.getServiceUrl(serviceCible);
        if (url == null) {
            System.err.println("Service " + serviceCible + " introuvable dans Consul !");
            return;
        }

        try {
            restTemplate.postForEntity(url + "/messages", message, Void.class);
        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du message à " + serviceCible + " : " + e.getMessage());
        }
    }
}
