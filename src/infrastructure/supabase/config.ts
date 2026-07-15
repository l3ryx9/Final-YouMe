/**
 * Configuration Supabase
 *
 * Variables d'environnement requises (dans .env ou eas.json) :
 *   EXPO_PUBLIC_SUPABASE_URL   — URL de votre projet Supabase
 *   EXPO_PUBLIC_SUPABASE_ANON_KEY — Clé publique (anon) du projet
 */
import { createClient } from '@supabase/supabase-js';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Database } from './database.types';

const SUPABASE_URL = process.env.EXPO_PUBLIC_SUPABASE_URL ?? '';
const SUPABASE_ANON_KEY = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY ?? '';

if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
  console.warn(
    '[Supabase] EXPO_PUBLIC_SUPABASE_URL ou EXPO_PUBLIC_SUPABASE_ANON_KEY manquant. ' +
    'Définissez ces variables dans votre fichier .env.'
  );
}

export const supabase = createClient<Database>(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: false,
  },
});

/** Noms des tables Supabase (équivalents des COLLECTIONS Firestore) */
export const TABLES = {
  USERS: 'users',
  USERNAMES: 'usernames',
  PUBLIC_PROFILES: 'public_profiles',
  CONVERSATIONS: 'conversations',
  MESSAGES: 'messages',
  PARTNER_REQUESTS: 'partner_requests',
  PARTNERS: 'partners',
  LOCATION_SHARES: 'location_shares',
  LOCATION_REQUESTS: 'location_requests',
  STEALTH_TRACKING: 'stealth_tracking',
  APP_LOGS: 'app_logs',
} as const;

/** Noms des buckets Supabase Storage */
export const BUCKETS = {
  AVATARS: 'avatars',
  TEMP_MEDIA: 'temp-media',
} as const;
