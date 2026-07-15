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
| **Gemini 1.5 Flash** (API) | — | Analyse approfondie des red/green flags relationnels |

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
```

### 2. Configurer Supabase

1. Créer un projet sur [supabase.com](https://supabase.com)
2. Activer : **Authentication** (Email/Password), **Realtime**, **Storage**
3. Exécuter les migrations SQL dans `supabase/migrations/`
4. Renseigner les variables dans `.env` :

```bash
EXPO_PUBLIC_SUPABASE_URL=https://xxxx.supabase.co
EXPO_PUBLIC_SUPABASE_ANON_KEY=your_anon_key
```

### 3. Configurer Gemini (optionnel — analyse approfondie)

1. Créer une clé sur [aistudio.google.com](https://aistudio.google.com)
2. Ajouter dans `.env` :

```bash
EXPO_PUBLIC_GEMINI_API_KEY=your_gemini_api_key
EXPO_PUBLIC_GEMINI_MODEL=gemini-1.5-flash
```

> ℹ️ L'application fonctionne **sans Gemini** avec un fallback heuristique automatique.

### 4. Lancer l'application

```bash
npx expo start          # QR code pour Expo Go
npx expo start --android # Émulateur Android
npx expo start --ios    # Simulateur iOS
```

---

## Modèles IA embarqués

Les modèles sont **compilés dans le binaire de l'application** via Metro (pas de téléchargement requis). Ils sont situés dans `assets/ai-models/` et copiés vers le stockage local au premier lancement.

```
assets/ai-models/
├── emotion/      ← DistilBERT Emotion (tokenizer + model.onnx)
├── whisper/      ← Whisper Tiny (encoder + decoder ONNX)
└── llm/          ← Qwen2.5-1.5B-Instruct q4f16 (tokenizer + model.onnx)
```

---

## Tests

```bash
yarn test                  # Tests unitaires
yarn test --watch          # Mode watch
yarn test --coverage       # Rapport de couverture
```

Les tests couvrent :
- Validateurs Zod (loginSchema, registerSchema, passwordStrength)
- EmotionAnalysisService (heuristique + ONNX mock)
- LLMService (extraction rule-based)
- InconsistencyDetector (patterns de contradiction)
- VoiceMessageStorage (Expo FileSystem mock)

---

## Build Production (EAS Build)

```bash
eas login
eas build:configure

# Build Android APK de preview
eas build --profile preview --platform android

# Build Android AAB pour Play Store
eas build --profile production --platform android

# Build iOS pour App Store
eas build --profile production --platform ios
```

---

## Structure du Projet

```
app/                  ← Écrans Expo Router
  (app)/              ← Écrans authentifiés
    (tabs)/           ← Onglets principaux (discussions, partenaires, recherche, paramètres)
    chat/[id].tsx     ← Écran de chat avec jauge IA
    flags/[id].tsx    ← Signaux relationnels (red/green flags)
    analysis/[id].tsx ← Analyse IA détaillée
  (auth)/             ← Écrans de connexion / inscription
src/
  ai/                 ← Moteurs IA (Whisper, DistilBERT, Qwen, Gemini)
  domain/             ← Entités et interfaces (Clean Architecture)
  infrastructure/     ← Supabase, stockage local, notifications
  presentation/       ← Composants, hooks, stores Zustand
assets/
  ai-models/          ← Modèles IA embarqués
  images/             ← Assets visuels
supabase/
  migrations/         ← Schéma SQL
```

Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour la documentation technique complète.

---

## Éthique & IA

YouMe respecte les principes suivants :

- **Probabiliste, jamais certain** : toutes les analyses IA sont présentées avec des formulations probabilistes ("potentielle", "suggère", "possible")
- **Citations obligatoires** : chaque extraction est justifiée par une citation exacte du message source
- **Zéro hallucination** : les modèles n'inventent pas d'informations
- **Séparation faits/interprétations** : l'UI distingue les observations vérifiables des hypothèses
- **Vérification manuelle recommandée** pour toute incohérence détectée

---

## Licence

MIT — Voir [LICENSE](LICENSE)

---

## Contribuer

Voir [CONTRIBUTING.md](CONTRIBUTING.md)
