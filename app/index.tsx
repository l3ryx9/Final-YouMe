/**
 * Route d'entrée « / » — Expo Router
 *
 * Rôle : donner un écran correspondant à l'URL initiale « / » et rediriger
 * immédiatement vers le bon groupe de routes selon l'état d'authentification
 * ET l'état de préparation de l'IA locale.
 *
 * Pourquoi ce fichier existe :
 *   Sans lui, « / » ne correspond à aucun écran (il n'y a que les groupes
 *   (auth) et (app)). L'app se retrouvait alors à naviguer « à la main »
 *   depuis le layout racine avant que le navigateur soit monté — ce qui
 *   fait planter l'app juste après l'intro. Ici, on utilise le composant
 *   <Redirect> d'Expo Router, qui attend que la navigation soit prête avant
 *   de rediriger : plus de crash, et le login/inscription s'affiche.
 *
 * FIX : le téléchargement des modèles IA locaux doit se faire AVANT la page
 * d'inscription/connexion, et non plus seulement après la première
 * connexion. On vérifie donc `areAllModelsReady()` ici, en même temps que
 * l'état d'auth, et on redirige vers l'écran de téléchargement si besoin —
 * quel que soit l'état d'authentification. Le téléchargement lui-même est
 * déjà lancé en arrière-plan dès le montage de app/_layout.tsx ; cet écran
 * ne fait qu'attendre/afficher sa progression avant de laisser passer
 * l'utilisateur. Une fois les modèles prêts (ou l'échec assumé), l'écran
 * `(auth)/download-models` redirige à son tour vers login ou vers les tabs
 * selon l'état d'authentification.
 *
 * L'intro animée « YouMe » reste gérée par app/_layout.tsx (overlay plein
 * écran) : cet écran peut donc ne rien rendre de visible (null).
 */
import { Redirect } from 'expo-router';
import React, { useEffect, useState } from 'react';
import { View, Image, StyleSheet } from 'react-native';
import { useAuthStore } from '@presentation/stores/authStore';
import { modelDownloadManager } from '@ai/models/ModelDownloadManager';

export default function Index() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const isInitialized   = useAuthStore((s) => s.isInitialized);

  // null = pas encore vérifié. Vérifié une seule fois dès que l'état d'auth
  // est connu, pour ne pas retarder inutilement le premier check Supabase.
  const [modelsReady, setModelsReady] = useState<boolean | null>(null);

  useEffect(() => {
    if (!isInitialized) return;
    let cancelled = false;
    modelDownloadManager
      .areAllModelsReady()
      .then((ready) => { if (!cancelled) setModelsReady(ready); })
      .catch(() => { if (!cancelled) setModelsReady(false); });
    return () => { cancelled = true; };
  }, [isInitialized]);

  // Affiche le logo-splash pendant que Supabase vérifie la session ET que
  // l'état des modèles IA est déterminé. Évite le clignotement de
  // login/téléchargement au démarrage.
  if (!isInitialized || modelsReady === null) {
    return (
      <View style={styles.splash}>
        <Image
          source={require('../assets/images/logo-splash.png')}
          style={styles.splashImage}
          resizeMode="contain"
        />
      </View>
    );
  }

  if (!modelsReady) {
    return <Redirect href="/(auth)/download-models" />;
  }

  return (
    <Redirect href={isAuthenticated ? '/(app)/(tabs)' : '/(auth)/login'} />
  );
}

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    backgroundColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
  },
  splashImage: {
    width: '100%',
    height: '100%',
  },
});
