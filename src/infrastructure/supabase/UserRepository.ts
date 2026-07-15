/**
 * Repository utilisateurs — Supabase Postgres
 * Remplace src/infrastructure/firebase/UserRepository.ts
 */
import { supabase, TABLES } from './config';
import type { Database } from './database.types';
import type { User } from '@domain/entities/User';
import { logInfo, logError, logWarn } from '@shared/utils/logger';

const CTX = 'UserRepository';

function rowToUser(row: any): User {
  return {
    id: row.id,
    email: row.email,
    username: row.username,
    displayName: row.display_name,
    photoURL: row.photo_url ?? undefined,
    bio: row.bio ?? undefined,
    isOnline: row.is_online ?? false,
    lastSeen: row.last_seen ? new Date(row.last_seen) : new Date(),
    createdAt: new Date(row.created_at),
    updatedAt: new Date(row.updated_at),
    isEmailVerified: row.is_email_verified ?? false,
    aiEnabled: row.ai_enabled ?? true,
    fcmToken: row.fcm_token ?? undefined,
    e2ePublicKey: row.e2e_public_key ?? undefined,
  };
}

class SupabaseUserRepository {
  /**
   * Vérifie si un username est disponible.
   */
  async isUsernameAvailable(username: string): Promise<boolean> {
    logInfo(`${CTX}.isUsernameAvailable`, { username });
    try {
      const { data, error } = await supabase
        .from(TABLES.USERNAMES)
        .select('username')
        .eq('username', username.toLowerCase())
        .maybeSingle();
      if (error) throw error;
      const available = data === null;
      logInfo(`${CTX}.isUsernameAvailable:✓`, { username, available });
      return available;
    } catch (err: any) {
      logError(`${CTX}.isUsernameAvailable`, err);
      throw err;
    }
  }

  /**
   * Crée un utilisateur après inscription (profil + réservation du username).
   */
  async createUser(params: {
    id: string;
    email: string;
    username: string;
    displayName: string;
    publicKeyB64?: string;
  }): Promise<User> {
    logInfo(`${CTX}.createUser`, { username: params.username, email: params.email });
    try {
      const now = new Date().toISOString();
      const userRow = {
        id: params.id,
        email: params.email,
        username: params.username.toLowerCase(),
        display_name: params.displayName,
        photo_url: null,
        bio: null,
        is_online: true,
        last_seen: now,
        is_email_verified: false,
        ai_enabled: true,
        fcm_token: null,
        native_fcm_token: null,
        e2e_public_key: params.publicKeyB64 ?? null,
        created_at: now,
        updated_at: now,
      };

      const { error: userError } = await supabase.from(TABLES.USERS).insert(userRow);
      if (userError) throw new Error(`Erreur création utilisateur : ${userError.message}`);

      const { error: usernameError } = await supabase
        .from(TABLES.USERNAMES)
        .insert({ username: params.username.toLowerCase(), uid: params.id });
      if (usernameError) throw new Error(`Username déjà utilisé : ${usernameError.message}`);

      await supabase.from(TABLES.PUBLIC_PROFILES).insert({
        id: params.id,
        username: params.username.toLowerCase(),
        display_name: params.displayName,
        photo_url: null,
        bio: null,
        is_online: true,
        last_seen: now,
        e2e_public_key: params.publicKeyB64 ?? null,
      });

      logInfo(`${CTX}.createUser:✓`, { id: params.id, username: params.username });
      return rowToUser(userRow);
    } catch (err: any) {
      logError(`${CTX}.createUser`, err);
      throw err;
    }
  }

  /**
   * Récupère un utilisateur par son ID.
   */
  async getUserById(userId: string): Promise<User | null> {
    logInfo(`${CTX}.getUserById`, { userId });
    try {
      const { data, error } = await supabase
        .from(TABLES.USERS)
        .select('*')
        .eq('id', userId)
        .maybeSingle();
      if (error) throw new Error(`Erreur lecture utilisateur : ${error.message}`);
      if (!data) {
        logWarn(`${CTX}.getUserById:notFound`, { userId });
        return null;
      }
      logInfo(`${CTX}.getUserById:✓`, { userId, username: data.username });
      return rowToUser(data);
    } catch (err: any) {
      logError(`${CTX}.getUserById`, err);
      throw err;
    }
  }

  /**
   * Récupère un utilisateur par son username (depuis le profil public).
   */
  async getUserByUsername(username: string): Promise<User | null> {
    logInfo(`${CTX}.getUserByUsername`, { username });
    try {
      const { data, error } = await supabase
        .from(TABLES.PUBLIC_PROFILES)
        .select('*')
        .eq('username', username.toLowerCase())
        .maybeSingle();
      if (error) throw new Error(`Erreur recherche username : ${error.message}`);
      if (!data) {
        logWarn(`${CTX}.getUserByUsername:notFound`, { username });
        return null;
      }
      logInfo(`${CTX}.getUserByUsername:✓`, { username, id: data.id });
      return {
        id: data.id,
        email: '',
        username: data.username,
        displayName: data.display_name,
        photoURL: data.photo_url ?? undefined,
        bio: data.bio ?? undefined,
        isOnline: data.is_online ?? false,
        lastSeen: data.last_seen ? new Date(data.last_seen) : new Date(),
        createdAt: new Date(),
        updatedAt: new Date(),
        isEmailVerified: true,
        aiEnabled: true,
        e2ePublicKey: data.e2e_public_key ?? undefined,
      };
    } catch (err: any) {
      logError(`${CTX}.getUserByUsername`, err);
      throw err;
    }
  }

  /**
   * Met à jour le profil utilisateur.
   */
  async updateUser(userId: string, updates: Partial<User>): Promise<void> {
    const fields = Object.keys(updates).filter((k) => k !== 'id');
    logInfo(`${CTX}.updateUser`, { userId, fields });
    try {
      const dbUpdates: Database['public']['Tables']['users']['Update'] = { updated_at: new Date().toISOString() };
      if (updates.displayName !== undefined) dbUpdates.display_name = updates.displayName;
      // Accepte photoURL (entité) et photoUrl (ancienne forme) pour compatibilité
      const photoVal = (updates as any).photoURL ?? (updates as any).photoUrl;
      if (photoVal !== undefined) dbUpdates.photo_url = photoVal;
      if (updates.bio !== undefined) dbUpdates.bio = updates.bio;
      if (updates.aiEnabled !== undefined) dbUpdates.ai_enabled = updates.aiEnabled;

      const { error } = await supabase.from(TABLES.USERS).update(dbUpdates).eq('id', userId);
      if (error) throw new Error(`Erreur mise à jour utilisateur : ${error.message}`);

      // Mettre à jour le profil public aussi
      const publicUpdates: Database['public']['Tables']['public_profiles']['Update'] = {};
      if (updates.displayName !== undefined) publicUpdates.display_name = updates.displayName;
      if (photoVal !== undefined) publicUpdates.photo_url = photoVal;
      if (updates.bio !== undefined) publicUpdates.bio = updates.bio;
      if (Object.keys(publicUpdates).length > 0) {
        const { error: pubErr } = await supabase
          .from(TABLES.PUBLIC_PROFILES)
          .update(publicUpdates)
          .eq('id', userId);
        if (pubErr) logWarn(`${CTX}.updateUser:publicProfile`, { error: pubErr.message });
      }

      logInfo(`${CTX}.updateUser:✓`, { userId, dbFields: Object.keys(dbUpdates) });
    } catch (err: any) {
      logError(`${CTX}.updateUser`, err);
      throw err;
    }
  }

  /**
   * Active ou désactive l'analyse IA pour un utilisateur.
   */
  async updateAiEnabled(userId: string, enabled: boolean): Promise<void> {
    logInfo(`${CTX}.updateAiEnabled`, { userId, enabled });
    try {
      const { error } = await supabase.from(TABLES.USERS).update({
        ai_enabled: enabled,
        updated_at: new Date().toISOString(),
      }).eq('id', userId);
      if (error) throw new Error(`Erreur updateAiEnabled : ${error.message}`);
      logInfo(`${CTX}.updateAiEnabled:✓`, { userId, enabled });
    } catch (err: any) {
      logError(`${CTX}.updateAiEnabled`, err);
      throw err;
    }
  }

  /**
   * Met à jour le statut en ligne de l'utilisateur.
   */
  async updateOnlineStatus(userId: string, isOnline: boolean): Promise<void> {
    logInfo(`${CTX}.updateOnlineStatus`, { userId, isOnline });
    try {
      const now = new Date().toISOString();
      const { error } = await supabase.from(TABLES.USERS).update({
        is_online: isOnline,
        last_seen: now,
        updated_at: now,
      }).eq('id', userId);
      if (error) throw new Error(`Erreur updateOnlineStatus : ${error.message}`);
      await supabase.from(TABLES.PUBLIC_PROFILES).update({
        is_online: isOnline,
        last_seen: now,
      }).eq('id', userId);
      logInfo(`${CTX}.updateOnlineStatus:✓`, { userId, isOnline });
    } catch (err: any) {
      logError(`${CTX}.updateOnlineStatus`, err);
      throw err;
    }
  }

  /**
   * Met à jour le token FCM pour les notifications push.
   */
  async updateFcmToken(userId: string, token: string): Promise<void> {
    logInfo(`${CTX}.updateFcmToken`, { userId });
    try {
      const { error } = await supabase.from(TABLES.USERS).update({
        fcm_token: token,
        updated_at: new Date().toISOString(),
      }).eq('id', userId);
      if (error) throw new Error(`Erreur updateFcmToken : ${error.message}`);
      logInfo(`${CTX}.updateFcmToken:✓`, { userId });
    } catch (err: any) {
      logError(`${CTX}.updateFcmToken`, err);
      throw err;
    }
  }

  /**
   * Met à jour le token FCM natif (@react-native-firebase/messaging).
   */
  async updateNativeFcmToken(userId: string, token: string): Promise<void> {
    logInfo(`${CTX}.updateNativeFcmToken`, { userId });
    try {
      const { error } = await supabase.from(TABLES.USERS).update({
        native_fcm_token: token,
        updated_at: new Date().toISOString(),
      }).eq('id', userId);
      if (error) throw new Error(`Erreur updateNativeFcmToken : ${error.message}`);
      logInfo(`${CTX}.updateNativeFcmToken:✓`, { userId });
    } catch (err: any) {
      logError(`${CTX}.updateNativeFcmToken`, err);
      throw err;
    }
  }

  /**
   * Publie la clé publique E2E de l'utilisateur.
   */
  async publishE2EPublicKey(userId: string, publicKeyB64: string): Promise<void> {
    logInfo(`${CTX}.publishE2EPublicKey`, { userId });
    try {
      const { error } = await supabase.from(TABLES.USERS).update({
        e2e_public_key: publicKeyB64,
        updated_at: new Date().toISOString(),
      }).eq('id', userId);
      if (error) throw new Error(`Erreur publishE2EPublicKey : ${error.message}`);
      await supabase.from(TABLES.PUBLIC_PROFILES).update({
        e2e_public_key: publicKeyB64,
      }).eq('id', userId);
      logInfo(`${CTX}.publishE2EPublicKey:✓`, { userId });
    } catch (err: any) {
      logError(`${CTX}.publishE2EPublicKey`, err);
      throw err;
    }
  }

  /**
   * Récupère la clé publique E2E d'un utilisateur.
   */
  async getE2EPublicKey(userId: string): Promise<string | null> {
    logInfo(`${CTX}.getE2EPublicKey`, { userId });
    try {
      const { data, error } = await supabase
        .from(TABLES.PUBLIC_PROFILES)
        .select('e2e_public_key')
        .eq('id', userId)
        .maybeSingle();
      if (error) throw new Error(`Erreur getE2EPublicKey : ${error.message}`);
      const key = data?.e2e_public_key ?? null;
      logInfo(`${CTX}.getE2EPublicKey:✓`, { userId, found: key !== null });
      return key;
    } catch (err: any) {
      logError(`${CTX}.getE2EPublicKey`, err);
      throw err;
    }
  }

  /**
   * Supprime toutes les données de l'utilisateur.
   */
  async deleteUser(userId: string): Promise<void> {
    logInfo(`${CTX}.deleteUser`, { userId });
    try {
      await supabase.from(TABLES.PUBLIC_PROFILES).delete().eq('id', userId);
      await supabase.from(TABLES.USERNAMES).delete().eq('uid', userId);
      const { error } = await supabase.from(TABLES.USERS).delete().eq('id', userId);
      if (error) throw new Error(`Erreur deleteUser : ${error.message}`);
      logInfo(`${CTX}.deleteUser:✓`, { userId });
    } catch (err: any) {
      logError(`${CTX}.deleteUser`, err);
      throw err;
    }
  }

  /**
   * Recherche des profils publics par username (recherche partielle).
   */
  async searchUsersByUsername(query: string, currentUserId: string): Promise<User[]> {
    logInfo(`${CTX}.searchUsersByUsername`, { query, currentUserId });
    try {
      const { data, error } = await supabase
        .from(TABLES.PUBLIC_PROFILES)
        .select('*')
        .ilike('username', `%${query.toLowerCase()}%`)
        .neq('id', currentUserId)
        .limit(20);
      if (error) throw new Error(`Erreur recherche : ${error.message}`);
      const results = (data ?? []).map((row) => ({
        id: row.id,
        email: '',
        username: row.username,
        displayName: row.display_name,
        photoURL: row.photo_url ?? undefined,
        bio: row.bio ?? undefined,
        isOnline: row.is_online ?? false,
        lastSeen: row.last_seen ? new Date(row.last_seen) : new Date(),
        createdAt: new Date(),
        updatedAt: new Date(),
        isEmailVerified: true,
        aiEnabled: true,
        e2ePublicKey: row.e2e_public_key ?? undefined,
      }));
      logInfo(`${CTX}.searchUsersByUsername:✓`, { query, count: results.length });
      return results;
    } catch (err: any) {
      logError(`${CTX}.searchUsersByUsername`, err);
      throw err;
    }
  }
}

export const userRepository = new SupabaseUserRepository();
