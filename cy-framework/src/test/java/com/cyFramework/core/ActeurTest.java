package com.cyFramework.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActeurTest {

    @Test
    void testFIFO() {
        class TestActeur extends Acteur {
            public Message lastMsg = null;

            TestActeur() {
                super("ActeurFIFO");
            }

            @Override
            public void recevoirMessage(Message message) {
                lastMsg = message;
            }
        }

        TestActeur acteur = new TestActeur();
        acteur.demarrer();

        Message m1 = new Message("A", "ActeurFIFO", "Msg1");
        Message m2 = new Message("B", "ActeurFIFO", "Msg2");

        acteur.ajouterMessage(m1);
        acteur.ajouterMessage(m2);

        acteur.tick();
        assertEquals("Msg1", acteur.lastMsg.getContenu());

        acteur.tick();
        assertEquals("Msg2", acteur.lastMsg.getContenu());
    }
}
