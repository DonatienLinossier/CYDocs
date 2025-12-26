CYDocs est une plateforme distribuée de gestion et d'édition de documents collaboratifs. Ce projet a été réalisé pour démontrer la mise en œuvre d'une architecture microservices, de la découverte de services et de la synchronisation de données en temps réel.

# 🏗️ Architecture du Système
Le système repose sur un découpage strict entre le client et l'écosystème backend :

Frontend : Une application web réactive développée avec React, HTML et CSS.

Gateway (Passerelle) : Nginx sert de point d'entrée unique, filtrant et routant toutes les requêtes vers les services appropriés.

Découverte de Services : Consul et Eureka gèrent l'enregistrement dynamique et la localisation des instances de services.

Synchronisation Temps Réel : Utilisation des WebSockets pour permettre une collaboration instantanée sur un même document.

Persistance : Une base de données centralisée partagée pour garantir la cohérence des données.

Microservices Backend
Document Management Service : Logique de création, stockage et gestion des documents.

User Management Service : Gestion des profils utilisateurs et des comptes.

CY-Framework : Librairie interne partagée pour les utilitaires communs.

# 📈 Scalabilité des Conteneurs
L'architecture microservices de CYDocs est conçue pour la scalabilité horizontale. Grâce à l'utilisation de conteneurs et d'un registre de services (Eureka), il est possible de multiplier les instances d'un service spécifique pour répondre à une charge accrue.

Déploiement Scalable : Chaque microservice peut être répliqué indépendamment sans affecter le reste du système.

Équilibrage de Charge : La Gateway et le système de découverte de services répartissent automatiquement le trafic entre les différentes instances actives d'un même service.

Commande de Mise à l'Échelle : Pour augmenter le nombre d'instances d'un service (ex: document-service) :

podman-compose up -d --scale document-service=3
#✨ Showcase du Projet
##📝 Création de Compte
Un processus d'inscription fluide pour accéder rapidement à l'espace collaboratif.

## 📄 Édition et Synchronisation Temps Réel
Grâce aux WebSockets, les modifications sont répercutées instantanément sur tous les écrans connectés.

##  📊 Tableau de Bord Utilisateur
Gestion centralisée des documents et visualisation de l'activité récente.

##  🔐 Gestion des Accès
Contrôle précis des permissions (lecture/écriture) pour chaque collaborateur.

#🛠️ Installation et Prérequis
1. Installer Podman & Compose
Windows/macOS : Téléchargez Podman Desktop. Après installation, initialisez l'environnement :

Bash

podman machine init && podman machine start
Linux : Installez via votre gestionnaire de paquets (ex: sudo apt install podman sur Ubuntu).

Podman Compose : Installez l'outil via Python :

Bash

pip install podman-compose
2. Lancer le Projet
Bash

git clone https://github.com/DonatienLinossier/CYDocs.git
cd CYDocs
podman-compose up -d
