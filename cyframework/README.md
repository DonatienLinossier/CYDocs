# CY-FRAMEWORK

## Comment l'intégrer ?

### 1. Ajouter la dépendance Maven
```xml
<dependency>
  <groupId>com.cyframework</groupId>
  <artifactId>cy-framework</artifactId>
  <version>1.0.0</version>
  <scope>system</scope>
  <systemPath>${project.basedir}/libs/mon-framework-1.0.0.jar</systemPath>
</dependency>
```

### 2. Configurer Consul
Pour la configuration de Consul, reportez-vous à notre leader suprême Donatien

---

## Où placer le JAR dans le microservice

- **Méthode locale (install Maven)** :rien à faire, Maven s’en charge automatiquement.  

- **Méthode "libs"** : créez ce dossier dans votre microservice :
```
service-utilisateur/
 ├─ src/
 ├─ pom.xml
 └─ libs/
     └─ mon-framework-1.0.0.jar
```

---

## Comment l'utiliser ?

### Créer un acteur
```java
@Component
public class ActeurUtilisateur extends Acteur {
    public ActeurUtilisateur() { 
        super("ActeurUtilisateur"); 
    }

    @Override
    public void recevoirMessage(Message msg) {
        getLogger().info("Reçu : " + msg.getContenu());
    }
}
```

### Envoyer un message
```java
Message msg = new Message(getNom(), "ActeurDocument", "Bonjour !");
envoyerMessage("service-document", msg);
```

---

## Résumé des principales méthodes

| Élément            | Rôle                                     |
|--------------------|-----------------------------------------|
| `Acteur`           | Classe de base pour les composants logiques |
| `envoyerMessage()` | Envoi local ou distant                  |
| `recevoirMessage()`| Réception d’un message                  |
| `Logger`           | Logging simple intégré                   |
| `Consul`           | Découverte et communication inter-service |
