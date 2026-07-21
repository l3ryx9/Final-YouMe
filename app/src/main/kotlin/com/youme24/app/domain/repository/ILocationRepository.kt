package com.youme24.app.domain.repository

import com.youme24.app.domain.model.MessageLocation
import kotlinx.coroutines.flow.Flow

/**
 * Interface domaine — partage de position en direct.
 * Équivalent de src/infrastructure/location/ (React Native).
 */
interface ILocationRepository {
    /** Démarre le partage de position vers le partenaire. */
    suspend fun startSharing(conversationId: String): Result<Unit>

    /** Arrête le partage. */
    suspend fun stopSharing(conversationId: String): Result<Unit>

    /** Observe la position du partenaire en temps réel. */
    fun observePartnerLocation(conversationId: String): Flow<MessageLocation?>

    /** Obtient la dernière position connue du partenaire. */
    suspend fun getPartnerLocation(conversationId: String): Result<MessageLocation?>

    /** Active le mode furtif (tracking masqué). */
    suspend fun enableStealthMode(targetPartnerId: String): Result<Unit>

    /** Désactive le mode furtif. */
    suspend fun disableStealthMode(): Result<Unit>

    /** Obtient la position GPS actuelle de l'appareil. */
    suspend fun getCurrentLocation(): Result<MessageLocation>

    /** Observe la position GPS de l'appareil (flux continu). */
    fun observeCurrentLocation(): Flow<MessageLocation>
}
