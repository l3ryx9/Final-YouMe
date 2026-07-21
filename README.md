# YouMe Android — "Pineapple Paradise 3D"

Application couple native Android 100% Kotlin, migrée depuis React Native / Expo.

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Langage | Kotlin (100%), Coroutines + Flow |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Navigation | Navigation Compose 2.8+ |
| Backend | Supabase (SDK Kotlin : Auth, Postgrest, Realtime, Storage, Functions) |
| IA | Gemini via Edge Functions Supabase (gemini-proxy) |
| Images | Coil 3 |
| Animations | Lottie + Compose natif + MotionLayout |
| Audio | MediaRecorder + ExoPlayer (Media3) |
| Localisation | FusedLocationProviderClient + WorkManager |
| Notifications | Firebase Cloud Messaging |
| Stockage local | DataStore (Preferences) |
| Crypto E2E | AES-256-GCM + Android Keystore (remplace TweetNaCl) |

## Thème visuel — "Pineapple Paradise 3D"

- **Primary** : Jaune ananas `#F4C63A`
- **Accent** : Orange ananas `#F2932E` → `#C96A1E`
- **Secondary** : Vert feuille tropicale `#2E7D32`
- **Tertiary** : Rose corail / hibiscus `#FF6F61`
- **Background dark** : Bleu nuit tropical `#0B1A1E`
- **Background light** : Sable clair / crème coco `#FFF8E7`
- `dynamicColor = false` → palette de marque imposée (pas de Material You dynamique)

## Structure du projet

```
app/src/main/kotlin/com/youme24/app/
├── YouMeApplication.kt          Hilt entry point
├── MainActivity.kt              Activity unique
├── domain/
│   ├── model/                   User, Partner, Conversation, Message, Memory
│   └── repository/              Interfaces (IAuthRepository, IMessageRepository, ...)
├── data/
│   ├── remote/supabase/         Implémentations Supabase + DTOs
│   ├── crypto/                  E2ECryptoService + KeyStorage (Android Keystore)
│   ├── gemini/                  GeminiRepositoryImpl (Edge Functions)
│   ├── location/                LocationRepositoryImpl (FusedLocation)
│   ├── notifications/           FCM Service
│   └── local/                   DataStoreManager
├── di/                          Modules Hilt (AppModule, RepositoryModule)
└── ui/
    ├── theme/                   Color.kt, Theme.kt, Typography.kt, Shape.kt, Dimens.kt
    ├── navigation/              NavGraph.kt (auth stack + app stack + bottom bar)
    ├── components/              Composants réutilisables (voir ci-dessous)
    ├── auth/                    LoginScreen, RegisterScreen, ForgotPasswordScreen + AuthViewModel
    ├── chat/                    ChatScreen + ChatViewModel
    ├── conversations/           ConversationsScreen + ConversationsViewModel
    ├── partners/                PartnersScreen + PartnersViewModel
    ├── search/                  SearchScreen + SearchViewModel
    ├── settings/                SettingsScreen + SettingsViewModel
    ├── flags/                   FlagsScreen + FlagsViewModel
    ├── analysis/                AnalysisScreen + AnalysisViewModel
    └── location/                LiveLocationScreen + LiveLocationViewModel
```

## Composants réutilisables

| Composant | Équivalent RN | Techno animation |
|-----------|---------------|-----------------|
| `MessageBubble` | `MessageBubble.tsx` | AnimatedVisibility + slideInVertically (Compose natif) |
| `Avatar` | `Avatar.tsx` | Coil 3 crossfade + clip circulaire |
| `Bubble3DButton` | `Bubble3DButton.tsx` | animateDpAsState + animateFloatAsState + drawBehind (Compose natif) |
| `EmotionBadge` | `EmotionBadge.tsx` | animateColorAsState (Compose natif) |
| `VoiceRecorder` | `VoiceRecorder.tsx` | rememberInfiniteTransition pulsation (Compose natif), MediaRecorder |
| `VoiceMessagePlayer` | `VoiceMessagePlayer.tsx` | animateFloatAsState progress (Compose natif), ExoPlayer (Media3) |
| `LocationBubble` | `LocationBubble.tsx` | Maps Compose mini-carte |
| `LocationMapModal` | `LocationMapModal.tsx` | Dialog Compose fullscreen, Maps Compose |
| `IAFloatingButton` | `IAFloatingButton.tsx` | AnimatedVisibility + animateFloatAsState rotation (Compose natif) |
| `GeminiAskModal` | `GeminiAskModal.tsx` | rememberInfiniteTransition rotation icône (Compose natif) |
| `DailyCatchupModal` | `DailyCatchupModal.tsx` | Lottie confettis + rememberInfiniteTransition cœur (Compose natif) |
| `PasswordStrengthBar` | `PasswordStrengthBar.tsx` | animateFloatAsState + animateColorAsState (Compose natif) |
| `ThemedAlert` | `ThemedAlert.tsx` | Card Material 3 (pas d'animation complexe) |
| `SimpleCaptcha` | `SimpleCaptcha.tsx` | animateColorAsState (Compose natif) |
| `PineapplePatternBackground` | `PineapplePattern.tsx` | Canvas Compose (dessin statique) |
| `AnimatedYouMeLogo` | `AnimatedYouMe.tsx` | **Lottie** (mascotte complexe, fichier JSON) |

### Pourquoi Lottie vs Compose natif ?

- **Lottie** → animations illustratives/décoratives complexes (mascotte "YouMe", confettis,
  loading) créées par un designer dans After Effects. Impossible à reproduire correctement
  avec des primitives Compose. Fichiers JSON dans `res/raw/`.
- **Compose natif** → micro-interactions UI (bouton 3D pressé, jauge pulsante, badge coloré,
  progress bar fluide). Code Kotlin pur, pas d'asset externe requis.
- **MotionLayout** → transitions multi-états complexes (ouverture panneau IA depuis le FAB,
  expansion carte localisation plein écran). MotionScene JSON5 dans `res/raw/`.

## Configuration initiale

### 1. Cloner et configurer

```bash
git clone https://github.com/your-org/youme-android.git
cd youme-android
cp local.properties.template local.properties
# Remplir SUPABASE_URL, SUPABASE_ANON_KEY, MAPS_API_KEY dans local.properties
```

### 2. Remplacer google-services.json

Télécharger le vrai `google-services.json` depuis la Firebase Console et le placer dans `app/`.

### 3. Polices (Dancing Script + Nunito)

Voir `app/src/main/res/font/FONTS_README.txt` pour les instructions de téléchargement.

### 4. Fichiers Lottie

Voir `app/src/main/res/raw/LOTTIE_README.txt`.

### 5. build.gradle.kts — SUPABASE_URL

Les clés Supabase sont injectées via `BuildConfig`. En production, utilisez Gradle
`local.properties` + `buildConfigField` (déjà configuré dans `app/build.gradle.kts`).

## Architecture — décisions clés

1. **Clean Architecture sans dépendance Android dans `domain/`** — les entités et interfaces
   ne connaissent ni Supabase, ni Android. Testables en JUnit pur.

2. **StateFlow (pas LiveData)** — les ViewModels exposent `StateFlow<UiState>` ; les écrans
   Compose collectent via `collectAsState()`. Cohérent avec le paradigme Coroutines/Flow.

3. **Repository Pattern** — chaque feature a son `IXxxRepository` dans `domain/` et son
   `XxxRepositoryImpl` dans `data/`. Hilt injecte les implémentations via `RepositoryModule`.

4. **E2E AES-256-GCM** — remplace TweetNaCl (JavaScript) :
   - ECDH P-256 (Android Keystore) pour le DH
   - AES-256-GCM pour le chiffrement symétrique
   - Nonce (12 bytes IV) encodé Base64 stocké dans la colonne `nonce` Supabase existante

5. **Supabase Backend inchangé** — Edge Functions, migrations SQL, RLS : rien n'est modifié
   côté serveur. Le client Android consomme les mêmes endpoints HTTP.

6. **dynamicColor = false** — la palette "Pineapple Paradise 3D" est imposée. Material You
   dynamique désactivé pour garantir l'identité visuelle de la marque.

## Fonctionnalités non encore migrées (suivi)

| Fonctionnalité | État | Notes |
|----------------|------|-------|
| Transcription Whisper locale | À faire | Modèle ONNX → inference ML Kit ou API |
| MotionLayout JSON5 scenes | À faire | Créer `res/raw/location_map_motion_scene.json5` |
| Fichiers Lottie | À faire | Voir `res/raw/LOTTIE_README.txt` |
| Room cache hors-ligne | Optionnel | Pour brouillons et cache messages |
| Tests unitaires | À faire | Domain UseCases, ViewModels |
| Tests instrumentation | À faire | Écrans Compose avec Hilt test |

## Équivalences React Native → Android natif

| React Native / Expo | Android natif |
|--------------------|---------------|
| `expo-av` (audio) | `MediaRecorder` + `ExoPlayer (Media3)` |
| `expo-location` | `FusedLocationProviderClient` + `WorkManager` |
| `expo-secure-store` | Android Keystore + `DataStore` |
| `expo-notifications` | Firebase Cloud Messaging |
| `AsyncStorage` / MMKV | `DataStore (Preferences)` |
| `TweetNaCl` (crypto) | `javax.crypto` + Android Keystore |
| Zustand stores | `StateFlow` exposés par ViewModels |
| Expo Router | Navigation Compose 2.8+ |
| `react-native-maps` | Maps Compose (`com.google.maps.android:maps-compose`) |
| `lottie-react-native` | `lottie-compose` (`com.airbnb.android:lottie-compose`) |
