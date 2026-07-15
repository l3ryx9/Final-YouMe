/**
 * Layout Racine — Expo Router
 * Configure le thème, les fonts, les providers globaux et l'état d'auth.
 * Migré de Firebase vers Supabase Auth.
 */
// Polyfill requis par `uuid` (utilisé par PartnerRepository, MessageRepository, etc.) :
// React Native/Hermes n'implémente pas crypto.getRandomValues() nativement.
// Doit être importé avant tout module qui importe 'uuid'.
import 'react-native-get-random-values';
import React, { useEffect, useRef, useCallback } from 'react';
import { themedAlert, ThemedAlertHost } from '@presentation/components/common/ThemedAlert';
import { View, AppState, AppStateStatus } from 'react-native';
import { Stack, router } from 'expo-router';
import { modelDownloadManager } from '../src/ai/models/ModelDownloadManager';
import { PaperProvider } from 'react-native-paper';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { authService } from '../src/infrastructure/supabase/AuthService';
import { userRepository } from '../src/infrastructure/supabase/UserRepository';
import { stealthLocationService } from '../src/infrastructure/location/StealthLocationService';
import { fcmLocationService } from '../src/infrastructure/location/FcmLocationService';
import { useAuthStore } from '../src/presentation/stores/authStore';
import { useUIStore } from '../src/presentation/stores/uiStore';
import { YOUME_DARK_THEME, YOUME_LIGHT_THEME } from '../src/shared/constants/theme';
import { aiOrchestrator } from '../src/ai/memory/AIOrchestrator';
import { notificationService } from '../src/infrastructure/notifications/NotificationService';
import { logError, formatErrorForUser, installGlobalErrorHandlers, configureRemoteLogging } from '../src/shared/utils/logger';
import { supabase, TABLES } from '../src/infrastructure/supabase/config';
import { Platform } from 'react-native';
import Constants from 'expo-constants';
import { PRESENCE_HEARTBEAT_MS } from '../src/shared/utils/presence';

installGlobalErrorHandlers();

// Le handler FCM arrière-plan/killed est dans index.js (root, Headless JS).
// Seul le handler onMessage (premier plan) est géré dans ce fichier.

// ── Remontée des erreurs vers Supabase ──────────────────────────────────────
// Chaque logError / logWarn sera inséré en base de façon fire-and-forget.
// Le user_id est lu dynamiquement dans le store au moment de l'envoi.
configureRemoteLogging(
  (entry, meta) => {
    supabase
      .from(TABLES.APP_LOGS)
      .insert({
        level:       entry.level,
        context:     entry.context,
        code:        entry.code ?? null,
        message:     entry.message,
        stack:       entry.stack ?? null,
        user_id:     meta.userId ?? null,
        platform:    meta.platform ?? null,
        app_version: meta.appVersion ?? null,
      })
      .then(
        () => {},
        () => { /* silencieux — ne jamais planter à cause du logger */ }
      );
  },
  () => ({
    userId:     useAuthStore.getState().user?.id,
    platform:   Platform.OS,
    appVersion: String(Constants.expoConfig?.version ?? Constants.manifest?.version ?? ''),
  })
);

SplashScreen.preventAutoHideAsync();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 2,
      staleTime: 30_000,
      gcTime: 5 * 60_000,
    },
  },
});

export default function RootLayout() {
  const { setUser, isAuthenticated, setInitialized } = useAuthStore();
  const { isDarkMode, loadPersistedState } = useUIStore();
  const theme = isDarkMode ? YOUME_DARK_THEME : YOUME_LIGHT_THEME;

  const isMounted = useRef(false);
  const loadingUid = useRef<string | null>(null);
  const stealthUnsubscribeRef = useRef<(() => void) | null>(null);
  const currentUidRef = useRef<string | null>(null);
  // FIX statut en ligne : heartbeat régulier tant que l'app est au premier
  // plan. AppState seul ne suffit pas — il ne se déclenche jamais si l'app
  // est tuée de force ou si le réseau coupe brutalement, ce qui laissait
  // `is_online` bloqué à `true` indéfiniment. Voir src/shared/utils/presence.ts.
  const heartbeatIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const stopPresenceHeartbeat = useCallback(() => {
    if (heartbeatIntervalRef.current) {
      clearInterval(heartbeatIntervalRef.current);
      heartbeatIntervalRef.current = null;
    }
  }, []);

  const startPresenceHeartbeat = useCallback(() => {
    stopPresenceHeartbeat();
    heartbeatIntervalRef.current = setInterval(() => {
      const uid = currentUidRef.current;
      if (!uid) return;
      userRepository
        .updateOnlineStatus(uid, true)
        .catch((err) => logError('RootLayout.presenceHeartbeat', err));
    }, PRESENCE_HEARTBEAT_MS);
  }, [stopPresenceHeartbeat]);

  const onLayoutRootView = useCallback(async () => {
    await SplashScreen.hideAsync();
  }, []);

  useEffect(() => {
    loadPersistedState();
    aiOrchestrator.initialize().catch((err) => logError('AIOrchestrator.initialize', err));

    // FIX : reprendre automatiquement un téléchargement de modèles IA
    // interrompu (app fermée/tuée en cours de téléchargement). Dès que
    // l'app est relancée, on relance downloadAllModels() — les fichiers
    // déjà terminés sont ignorés (état persisté sur disque), seul le
    // fichier en cours au moment de la fermeture est repris depuis le
    // début. Aucune interaction utilisateur requise.
    modelDownloadManager.downloadAllModels().catch((err) => {
      logError('RootLayout.resumeModelDownload', err);
    });

    // Supabase Auth — onAuthStateChange remplace onAuthStateChanged de Firebase
    const unsubscribe = authService.onAuthStateChanged(async (userId) => {
      if (userId) {
        const authState = useAuthStore.getState();

        if (authState.user?.id === userId) { setInitialized(); return; }
        if (authState.isLoading) return;
        if (loadingUid.current === userId) return;

        loadingUid.current = userId;
        try {
          const user = await userRepository.getUserById(userId);
          if (user) {
            setUser(user);
            currentUidRef.current = userId;
            setInitialized();
            await userRepository.updateOnlineStatus(userId, true);
            startPresenceHeartbeat();
            await notificationService.registerForPushNotifications(userId);
            await fcmLocationService.registerNativeFcmToken(userId);

            stealthUnsubscribeRef.current?.();
            stealthUnsubscribeRef.current = stealthLocationService.startListeningForStealthConfig(userId);
          } else {
            logError('RootLayout.profileMissing', {
              code: 'profile/not-found',
              message: `Profil Supabase introuvable pour ${userId}`,
            });
            themedAlert.alert(
              'Erreur',
              formatErrorForUser(
                { code: 'profile/not-found' },
                'Votre profil est introuvable. Veuillez réessayer de vous connecter.'
              )
            );
            setUser(null);
            setInitialized();
            await authService.logout();
          }
        } catch (error: any) {
          logError('RootLayout.loadUser', error);
          themedAlert.alert(
            'Erreur de connexion',
            formatErrorForUser(error, 'Impossible de charger votre profil. Veuillez réessayer.')
          );
          setUser(null);
          setInitialized();
          await authService.logout().catch(() => {});
        } finally {
          loadingUid.current = null;
        }
      } else {
        loadingUid.current = null;
        currentUidRef.current = null;
        setUser(null);
        setInitialized(); // pas connecté mais auth déterminé → affiche login sans flash
        stopPresenceHeartbeat();
        stealthUnsubscribeRef.current?.();
        stealthUnsubscribeRef.current = null;
        stealthLocationService.stopStealthTracking();
        fcmLocationService.stopTokenRefreshListener();
      }
    });

    // ── Firebase FCM — messages au premier plan ────────────────────────────
    let fcmUnsubscribe: (() => void) | null = null;
    try {
      const messaging = require('@react-native-firebase/messaging').default;
      fcmUnsubscribe = messaging().onMessage(async (remoteMessage: any) => {
        const data = remoteMessage?.data ?? {};
        // Les demandes de position furtive ne génèrent pas de notification visible
        if (data.type === 'stealth_location_request') return;
        // Notification standard reçue au premier plan → affichage local
        const title = remoteMessage?.notification?.title ?? data.title ?? 'YouMe';
        const body  = remoteMessage?.notification?.body  ?? data.body  ?? '';
        if (title || body) {
          await notificationService.scheduleLocalNotification(title, body, data);
        }
      });
    } catch { /* expo Go sans build natif */ }

    const handleAppStateChange = (nextState: AppStateStatus) => {
      const uid = currentUidRef.current;
      if (!uid) return;
      const isActive = nextState === 'active';
      userRepository
        .updateOnlineStatus(uid, isActive)
        .catch((err) => logError('RootLayout.updateOnlineStatus', err));
      // FIX statut en ligne : le heartbeat ne doit tourner que quand l'app
      // est réellement au premier plan, sinon on écrit `is_online = true`
      // en arrière-plan sans que l'utilisateur soit vraiment présent.
      if (isActive) {
        startPresenceHeartbeat();
      } else {
        stopPresenceHeartbeat();
      }
    };
    const appStateSubscription = AppState.addEventListener('change', handleAppStateChange);

    return () => {
      unsubscribe();
      appStateSubscription.remove();
      fcmUnsubscribe?.();
      stopPresenceHeartbeat();
      stealthUnsubscribeRef.current?.();
      stealthUnsubscribeRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!isMounted.current) {
      isMounted.current = true;
      return;
    }
    if (isAuthenticated) {
      // Première connexion : si les modèles IA ne sont pas encore téléchargés,
      // rediriger vers l'écran de téléchargement avant d'entrer dans l'app.
      modelDownloadManager.areAllModelsReady().then((ready) => {
        router.replace(ready ? '/(app)/(tabs)/' : '/(auth)/download-models');
      }).catch(() => {
        router.replace('/(app)/(tabs)/');
      });
    } else {
      router.replace('/(auth)/login');
    }
  }, [isAuthenticated]);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <QueryClientProvider client={queryClient}>
        <PaperProvider theme={theme}>
          <StatusBar style="light" backgroundColor="#221812" />
          <ThemedAlertHost />
          <View style={{ flex: 1 }} onLayout={onLayoutRootView}>
            <Stack screenOptions={{ headerShown: false }}>
              <Stack.Screen name="(auth)" options={{ headerShown: false }} />
              <Stack.Screen name="(app)" options={{ headerShown: false }} />
              <Stack.Screen name="debug-logs" options={{ headerShown: false, presentation: 'modal' }} />
            </Stack>
          </View>
        </PaperProvider>
      </QueryClientProvider>
    </GestureHandlerRootView>
  );
}
