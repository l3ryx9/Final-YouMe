# YouMe 🌿💬

Application de messagerie privée 1-to-1 avec **IA locale embarquée** — transcription vocale (Whisper Tiny), analyse émotionnelle (DistilBERT), analyse sémantique (Qwen2.5-1.5B), et détection d'incohérences relationnelles avec Gemini.

**Stack :** React Native + Expo SDK 51 · Supabase (Auth, Realtime, Storage) · Clean Architecture · SQLite · MMKV

---

## Fonctionnalités

### 💬 Messagerie
- Messages texte et vocaux en temps réel (Supabase Realtime)
- Accusés de réception et de lecture (✓✓)
- Suppression de messages
- Indicateur de statut en ligne
- Partage de position en direct (avec mode furtif)
- Envoi de photos et vidéos

### 🎙️ Messages Vocaux
- Enregistrement avec pause/reprise
- Stockage local (jamais envoyé en cloud)
- Lecture avec barre de progression et visualisation waveform
- Transcription automatique avec Whisper Tiny

### 🤖 Intelligence Artificielle — 3 modèles locaux + Gemini
Tous les modèles IA sont **directement embarqués dans l'application** (bundle natif). Aucun téléchargement requis.

| Modèle | Taille | Rôle |
|---|---|---|
| **Whisper Tiny** | ~241 Mo | Transcription des messages vocaux |
| **DistilBERT Emotion** | ~109 Mo | Analyse émotionnelle (6 émotions, scores probabilistes) |
| **Qwen2.5-1.5B-Instruct q4f16** | ~1.17 Go | Extraction d'entités (personnes, lieux, dates, tâches…) |
| **Gemini 2.5 Flash** (API) | — | Analyse approfondie des red/green flags relationnels |

#### Système d'analyse par points (jauge)
- L'analyse complète (3 IA locales + Gemini) s'effectue **toutes les 4 heures**
- Chaque conversation dispose d'une **jauge de santé relationnelle** (scores Red Flags / Green Flags)
- La jauge Gemini se met à jour **tous les 20 messages** dans une conversation
- Formulation éthique : probabiliste, jamais accusatoire

### 🔐 Sécurité & Confidentialité
- Authentification Supabase (email + vérification)
- CAPTCHA à l'inscription et connexion
- Règles Row Level Security strictes (données privées par défaut)
- Données IA 100% locales (SQLite, FileSystem)
- Export et suppression des données à la demande

---

## Installation Rapide

### Prérequis
- Node.js >= 20
- Expo CLI : `npm install -g expo-cli`
- EAS CLI : `npm install -g eas-cli`
- Un projet Supabase avec Auth et Realtime activés

### 1. Cloner et installer

```bash
git clone https://github.com/l3ryx9/new-youme.git
cd new-youme
yarn install
