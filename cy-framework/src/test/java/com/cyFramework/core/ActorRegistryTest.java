package com.cyFramework.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActorRegistryTest {

    @Test
    void testEnregistrerEtTrouver() {
        Acteur acteur = new Acteur("TestActeur") {
            @Override
            public void recevoirMessage(Message message) {}
        };

        ActorRegistry.enregistrer(acteur);

        Acteur result = ActorRegistry.trouver("TestActeur");

        assertNotNull(result);
        assertEquals("TestActeur", result.getNom());
    }

    @Test
    void testRetirer() {
        Acteur acteur = new Acteur("ToRemove") {
            @Override
            public void recevoirMessage(Message message) {}
        };

        ActorRegistry.enregistrer(acteur);
        ActorRegistry.retirer("ToRemove");

        assertNull(ActorRegistry.trouver("ToRemove"));
    }
}
