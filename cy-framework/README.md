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

---


# Communication entre microservices -- CY-Framework

CY-Framework fournit un système d'envoi de messages unifié permettant :

-   l'envoi **local** entre acteurs du même microservice\
-   l'envoi **inter-microservice** via **Consul + HTTP**

L'utilisateur **n'a pas besoin de gérer la différence** :\
`envoyerMessage()` s'occupe de tout automatiquement.

------------------------------------------------------------------------

## Fonctionnement interne

### 1. Envoi d'un message

Lorsqu'un acteur appelle :

``` java
envoyerMessage("service-document", msg);
```

le framework suit cette logique :

1.  **Cherche si l'acteur destinataire existe localement**

    -   S'il est trouvé → message **stocké dans la FIFO**
    -   traité plus tard via `tick()`

2.  **Sinon**, il interroge **Consul** pour l'URL du microservice

3.  Envoi HTTP :

```{=html}
<!-- -->
```
    POST http://<service-document>/messages

------------------------------------------------------------------------

## 2. Réception d'un message dans un microservice

Chaque microservice expose un endpoint :

``` java
@RestController
public class MessageController {

    @PostMapping("/messages")
    public void recevoirMessage(@RequestBody Message msg) {
        Acteur acteur = ActorRegistry.trouver(msg.getDestinataire());
        if (acteur != null) {
            acteur.ajouterMessage(msg); // FIFO
        }
    }
}
```

------------------------------------------------------------------------

## Exemple complet : Message inter-microservice

### Microservice A : Acteur émetteur

``` java
Message msg = new Message(
    "ActeurUtilisateur",
    "ActeurDocument",
    "L’utilisateur X a ouvert le document 42"
);

envoyerMessage("service-document", msg);
```

------------------------------------------------------------------------

### Microservice B : Contrôleur de réception

``` java
@PostMapping("/messages")
public void recevoirMessage(@RequestBody Message msg) {
    Acteur cible = ActorRegistry.trouver(msg.getDestinataire());
    if (cible != null) {
        cible.ajouterMessage(msg);
    }
}
```

------------------------------------------------------------------------

### Microservice B : Acteur récepteur

``` java
@Component
public class ActeurDocument extends Acteur {

    public ActeurDocument() {
        super("ActeurDocument");
    }

    @Override
    public void recevoirMessage(Message msg) {
        getLogger().info("Document mis à jour : " + msg.getContenu());
    }
}
```

------------------------------------------------------------------------

## Résultat global du flux

    ActeurUtilisateur (local)
        → MessageSender
            → HTTP via Consul
                → /messages (service-document)
                    → ActeurDocument.ajouterMessage()
                        → tick()
                            → recevoirMessage()


# Pour développer le framework

## Export en jar 

1. Se placer à la racine du framework (cy-framework)
2. (Optionnel) Changer dans le pom xml la version ou le artifactid 
3. Dans le terminal écrire : mvn clean package

## Tests junit

1. Se placer à la racine du framework (cy-framework)
2. Dans le terminal écrire : mvn test


