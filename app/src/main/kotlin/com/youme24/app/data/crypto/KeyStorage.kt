package com.youme24.app.data.crypto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.keyDataStore: DataStore<Preferences> by preferencesDataStore("e2e_keys")

/**
 * Stockage sécurisé des clés E2E — remplace expo-secure-store.
 *
 * La clé privée reste dans l'Android Keystore (gérée par E2ECryptoService).
 * Ce DataStore stocke uniquement des métadonnées non-sensibles (ex. public key propre).
 * Pour les données sensibles supplémentaires, utilisez EncryptedSharedPreferences.
 */
@Singleton
class KeyStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val PUBLIC_KEY_KEY = stringPreferencesKey("my_e2e_public_key")
        private val PARTNER_KEY_PREFIX = "partner_key_"
    }

    suspend fun saveMyPublicKey(publicKeyBase64: String) {
        context.keyDataStore.edit { prefs ->
            prefs[PUBLIC_KEY_KEY] = publicKeyBase64
        }
    }

    suspend fun getMyPublicKey(): String? =
        context.keyDataStore.data.map { it[PUBLIC_KEY_KEY] }.firstOrNull()

    suspend fun savePartnerPublicKey(partnerId: String, publicKeyBase64: String) {
        val key = stringPreferencesKey(PARTNER_KEY_PREFIX + partnerId)
        context.keyDataStore.edit { prefs -> prefs[key] = publicKeyBase64 }
    }

    suspend fun getPartnerPublicKey(partnerId: String): String? {
        val key = stringPreferencesKey(PARTNER_KEY_PREFIX + partnerId)
        return context.keyDataStore.data.map { it[key] }.firstOrNull()
    }

    suspend fun clearAll() {
        context.keyDataStore.edit { it.clear() }
    }
}
