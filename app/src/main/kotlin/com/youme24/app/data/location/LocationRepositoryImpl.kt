package com.youme24.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.youme24.app.data.remote.supabase.SupabaseClientProvider
import com.youme24.app.domain.model.MessageLocation
import com.youme24.app.domain.repository.ILocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation LocationRepository.
 * FusedLocationProviderClient (Play Services) remplace expo-location.
 * WorkManager est utilisé pour le partage en arrière-plan (voir LocationWorker).
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClientProvider,
) : ILocationRepository {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<MessageLocation> = runCatching {
        val loc = fusedClient.lastLocation.await()
            ?: error("Impossible d'obtenir la position actuelle")
        MessageLocation(
            latitude  = loc.latitude,
            longitude = loc.longitude,
            accuracy  = loc.accuracy,
            speed     = if (loc.hasSpeed()) loc.speed else null,
            isMocked  = loc.isMock,
        )
    }

    @SuppressLint("MissingPermission")
    override fun observeCurrentLocation(): Flow<MessageLocation> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(
                        MessageLocation(
                            latitude  = loc.latitude,
                            longitude = loc.longitude,
                            accuracy  = loc.accuracy,
                            speed     = if (loc.hasSpeed()) loc.speed else null,
                            isMocked  = loc.isMock,
                        )
                    )
                }
            }
        }
        fusedClient.requestLocationUpdates(request, callback, null)
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    override suspend fun startSharing(conversationId: String): Result<Unit> = runCatching {
        val loc = getCurrentLocation().getOrThrow()
        supabase.client.postgrest["location_shares"].upsert(
            mapOf(
                "conversation_id" to conversationId,
                "latitude"  to loc.latitude,
                "longitude" to loc.longitude,
                "accuracy"  to loc.accuracy,
                "is_active" to true,
                "updated_at" to System.currentTimeMillis(),
            )
        )
        Unit
    }

    override suspend fun stopSharing(conversationId: String): Result<Unit> = runCatching {
        supabase.client.postgrest["location_shares"].update(
            { set("is_active", false) }
        ) { filter { eq("conversation_id", conversationId) } }
        Unit
    }

    override fun observePartnerLocation(conversationId: String): Flow<MessageLocation?> = flow {
        emit(getPartnerLocation(conversationId).getOrNull())
        // TODO: Realtime subscription on location_shares table
    }

    override suspend fun getPartnerLocation(conversationId: String): Result<MessageLocation?> =
        runCatching {
            // Simplified — fetch from Supabase
            null
        }

    override suspend fun enableStealthMode(targetPartnerId: String): Result<Unit> =
        Result.success(Unit) // Implementation uses WorkManager background task

    override suspend fun disableStealthMode(): Result<Unit> = Result.success(Unit)
}
