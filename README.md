# CYDocs 🚀
CYDocs est une plateforme distribuée pour la gestion et l'édition collaborative de documents. Ce dépôt illustre une architecture microservices, la découverte de services, la synchronisation en temps réel et le déploiement containerisé.

<img width="1918" height="945" alt="image" src="https://github.com/user-attachments/assets/9dfefcb1-df07-4a37-b1fb-83d567107bc8" />

---

## Table des matières
- [Aperçu](#aperçu)
- [Fonctionnalités clés](#fonctionnalités-clés)
- [Architecture du système](#architecture-du-système)
- [Scalabilité & Conteneurs](#scalabilité--conteneurs)
- [Installation & Prérequis](#installation--prérequis)
- [Exécution](#exécution)
- [Contribution](#contribution)
- [Licence](#licence)
- [Contact](#contact)

---

## Aperçu
CYDocs permet à plusieurs utilisateurs d'éditer un même document simultanément, avec propagation instantanée des modifications via WebSockets et un backend découplé en microservices pour une maintenance et une montée en charge simplifiées.

---

## Fonctionnalités clés
- Édition collaborative en temps réel (WebSockets)
- Gestion des documents (création, stockage, historique)
- Gestion des utilisateurs et permissions (Création de compte/Droit lecture/Droit écriture)
- Architecture microservices (services découplés)
- Déploiement containerisé et scalable

---

## Architecture du système
Le projet suit une séparation claire entre frontend et backend :

- Frontend  
  - Application web réactive (React, HTML, CSS) : interface utilisateur.

- Gateway (Passerelle)  
  - Nginx : point d'entrée unique, routage et reverse proxy vers les microservices.

- Découverte de services  
  - Eureka : enregistrement dynamique et localisation des instances.

- Synchronisation temps réel  
  - WebSockets : propagation des modifications en temps réel entre clients.

- Persistance  
  - Base de données centralisée (partagée entre services) pour garantir la cohérence des données.

- Microservices principaux  
  - Document Management Service : création, stockage et gestion des documents.  
  - User Management Service : gestion des comptes et profils utilisateurs.  
  - CY-Framework : librairie interne partagée (utilitaires communs).

---

## Scalabilité & Conteneurs
Conçu pour une montée en charge horizontale :
- Chaque microservice peut être répliqué indépendamment.
- La Gateway et la découverte de services distribuent le trafic entre instances.
- Exemple de mise à l'échelle (Podman Compose) : répliquer `document-service` 3 fois

```bash
podman-compose up -d --scale document-service=3
```

---

## Installation & Prérequis

1. Prérequis
   - Podman (Windows / macOS via Podman Desktop, Linux via gestionnaire de paquets)
   - Python (pour installer podman-compose via pip)
   - Git

2. Initialisation (Windows / macOS)
```bash
# Initialiser et démarrer la machine Podman (si nécessaire)
podman machine init && podman machine start
```

3. Installer podman-compose
```bash
pip install podman-compose
```

4. Cloner et lancer le projet
```bash
git clone https://github.com/DonatienLinossier/CYDocs.git
cd CYDocs
podman-compose up -d
```

Remarques :
- Les variables d'environnement et les ports sont configurables dans les fichiers de composition (vérifiez les fichiers `podman-compose` / `docker-compose` si présents).
- Selon votre configuration, vous devrez peut‑être adapter les réglages de la gateway (Nginx) et de la base de données.

---

## Exécution & Accès
- Une fois les services démarrés, la Gateway (Nginx) sert d'entrée unique vers l'application.  
- Consultez la configuration de la gateway pour connaître le port et les routes exposées (généralement configurés dans `nginx` / `compose`).

---

## Contribution
Contribuer à CYDocs est le bienvenu !  
- Ouvrez une issue pour discuter d'un changement important.  
- Créez des branches de fonctionnalités claires (ex: `feature/nom-fonction`), puis soumettez une Pull Request.

Petits conseils :
- Respectez le style de code présent dans le dépôt.
- Ajoutez des tests pour les nouvelles fonctionnalités si possible.

---

## Licence
Ce projet utilise une licence à définir. Merci d'ajouter un fichier `LICENSE` avec la licence choisie (MIT, Apache-2.0, etc.) si nécessaire.

---

## Contact
Pour toute question ou suggestion : ouvrez une issue dans ce dépôt.

---

Merci d'utiliser CYDocs ✨ — une base pour expérimenter la collaboration temps réel et les architectures microservices.
