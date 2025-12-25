package com.cyFramework.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActeurTest {

    static class TestActeur extends Acteur {
        Message last = null;

        TestActeur() {
            super("ActeurTest");
        }

        @Override
        public void recevoirMessage(Message message) {
            last = message;
        }
    }

    @Test
    void testFIFO() {
        TestActeur acteur = new TestActeur();
        acteur.demarrer();

        Message m1 = new Message("A", "ActeurTest", "M1");
        Message m2 = new Message("B", "ActeurTest", "M2");

        acteur.ajouterMessage(m1);
        acteur.ajouterMessage(m2);

        acteur.tick();
        assertEquals("M1", acteur.last.getContenu());

        acteur.tick();
        assertEquals("M2", acteur.last.getContenu());
    }
    
    @Test
    void testFreezeActeur() {
        TestActeur acteur = new TestActeur();
        acteur.demarrer();
        acteur.freezeActeur(true);

        Message msg = new Message("A", "ActeurTest", "FreezeTest");
        acteur.ajouterMessage(msg);

        acteur.tick(); // ne doit rien faire
        assertNull(acteur.last);
    }

    @Test
    void testUnfreezeActeur() {
        TestActeur acteur = new TestActeur();
        acteur.demarrer();
        acteur.freezeActeur(true);

        Message msg = new Message("A", "ActeurTest", "UnfreezeTest");
        acteur.ajouterMessage(msg);

        acteur.tick();
        assertNull(acteur.last);

        acteur.freezeActeur(false);
        acteur.tick();

        assertNotNull(acteur.last);
        assertEquals("UnfreezeTest", acteur.last.getContenu());
    }

    @Test
    void testFlush() {
        TestActeur acteur = new TestActeur();
        acteur.demarrer();

        acteur.ajouterMessage(new Message("A", "ActeurTest", "M1"));
        acteur.ajouterMessage(new Message("B", "ActeurTest", "M2"));

        acteur.flush();

        acteur.tick();
        assertNull(acteur.last); // rien à traiter
    }
}
