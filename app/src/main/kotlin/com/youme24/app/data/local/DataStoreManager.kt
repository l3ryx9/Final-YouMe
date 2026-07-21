package com.youme24.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore("youme_prefs")

/**
 * Remplace AsyncStorage/MMKV du projet React Native.
 * Persistance des préférences utilisateur via Jetpack DataStore.
 */
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        val IS_DARK_MODE           = booleanPreferencesKey("is_dark_mode")
        val AI_ENABLED             = booleanPreferencesKey("ai_enabled")
        val NOTIFICATIONS_ENABLED  = booleanPreferencesKey("notifications_enabled")
        val IS_ONBOARDED           = booleanPreferencesKey("is_onboarded")
        val ACTIVE_TAB             = stringPreferencesKey("active_tab")
        val LAST_NOTIFICATION_ASKED = booleanPreferencesKey("notification_permission_asked")
    }

    val isDarkMode: Flow<Boolean>           = context.appDataStore.data.map { it[IS_DARK_MODE] ?: false }
    val isAiEnabled: Flow<Boolean>          = context.appDataStore.data.map { it[AI_ENABLED] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.appDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val isOnboarded: Flow<Boolean>          = context.appDataStore.data.map { it[IS_ONBOARDED] ?: false }
    val activeTab: Flow<String>             = context.appDataStore.data.map { it[ACTIVE_TAB] ?: "discussions" }

    suspend fun setDarkMode(enabled: Boolean) =
        context.appDataStore.edit { it[IS_DARK_MODE] = enabled }

    suspend fun setAiEnabled(enabled: Boolean) =
        context.appDataStore.edit { it[AI_ENABLED] = enabled }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        context.appDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }

    suspend fun setOnboarded(value: Boolean) =
        context.appDataStore.edit { it[IS_ONBOARDED] = value }

    suspend fun setActiveTab(tab: String) =
        context.appDataStore.edit { it[ACTIVE_TAB] = tab }

    suspend fun setNotificationPermissionAsked(asked: Boolean) =
        context.appDataStore.edit { it[LAST_NOTIFICATION_ASKED] = asked }

    suspend fun clearAll() = context.appDataStore.edit { it.clear() }
}
