package main.java.com.cyFramework.core;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ActorRegistry {

    private static final Map<String, Acteur> acteurs = new ConcurrentHashMap<>();

    public static void enregistrer(Acteur acteur) {
        acteurs.put(acteur.getNom(), acteur);
    }

    public static void retirer(String nom) {
        acteurs.remove(nom);
    }

    public static Acteur trouver(String nom) {
        return acteurs.get(nom);
    }
}
