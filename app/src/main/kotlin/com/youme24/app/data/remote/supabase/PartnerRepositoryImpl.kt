package com.youme24.app.data.remote.supabase

import com.youme24.app.domain.model.Partner
import com.youme24.app.domain.model.PartnerRequest
import com.youme24.app.domain.model.PartnerRequestStatus
import com.youme24.app.domain.repository.IPartnerRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider,
) : IPartnerRepository {

    private val db get() = supabase.client.postgrest
    private val currentUserId get() = supabase.client.auth.currentUserOrNull()?.id

    @Serializable
    private data class PartnerDto(
        @SerialName("user_id")              val userId: String,
        @SerialName("partner_id")           val partnerId: String,
        @SerialName("partner_username")     val partnerUsername: String,
        @SerialName("partner_display_name") val partnerDisplayName: String,
        @SerialName("partner_photo_url")    val partnerPhotoUrl: String? = null,
        @SerialName("partner_is_online")    val partnerIsOnline: Boolean = false,
        @SerialName("partner_last_seen")    val partnerLastSeen: Long? = null,
        @SerialName("conversation_id")      val conversationId: String,
        @SerialName("created_at")           val createdAt: Long = 0L,
    ) {
        fun toDomain() = Partner(
            userId = userId, partnerId = partnerId,
            partnerUsername = partnerUsername,
            partnerDisplayName = partnerDisplayName,
            partnerPhotoUrl = partnerPhotoUrl,
            partnerIsOnline = partnerIsOnline,
            partnerLastSeen = partnerLastSeen,
            conversationId = conversationId,
            createdAt = createdAt,
        )
    }

    @Serializable
    private data class PartnerRequestDto(
        @SerialName("id")                  val id: String,
        @SerialName("from_user_id")        val fromUserId: String,
        @SerialName("from_username")       val fromUsername: String,
        @SerialName("from_display_name")   val fromDisplayName: String,
        @SerialName("from_photo_url")      val fromPhotoUrl: String? = null,
        @SerialName("to_user_id")          val toUserId: String,
        @SerialName("status")              val status: String,
        @SerialName("created_at")          val createdAt: Long = 0L,
    ) {
        fun toDomain() = PartnerRequest(
            id = id, fromUserId = fromUserId,
            fromUsername = fromUsername,
            fromDisplayName = fromDisplayName,
            fromPhotoUrl = fromPhotoUrl,
            toUserId = toUserId,
            status = PartnerRequestStatus.valueOf(status.uppercase()),
            createdAt = createdAt,
        )
    }

    override suspend fun sendPartnerRequest(toUserId: String): Result<PartnerRequest> =
        runCatching {
            val uid = currentUserId ?: error("Not authenticated")
            // Fetch my profile for the request payload
            val me = db["users"].select { filter { eq("id", uid) } }
                .decodeSingle<com.youme24.app.data.remote.supabase.dto.UserDto>()
            db["partner_requests"].insert(
                mapOf(
                    "from_user_id"      to uid,
                    "from_username"     to me.username,
                    "from_display_name" to me.displayName,
                    "from_photo_url"    to me.photoUrl,
                    "to_user_id"        to toUserId,
                    "status"            to "pending",
                )
            ).decodeSingle<PartnerRequestDto>().toDomain()
        }

    override suspend fun acceptPartnerRequest(requestId: String): Result<Partner> =
        runCatching {
            // Call the RPC function accept_partner_request(request_id)
            db.rpc("accept_partner_request", buildJsonObject { put("request_id", requestId) })
            val uid = currentUserId ?: error("Not authenticated")
            db["partners"].select { filter { eq("user_id", uid) } }
                .decodeSingle<PartnerDto>().toDomain()
        }

    override suspend fun rejectPartnerRequest(requestId: String): Result<Unit> =
        runCatching {
            db["partner_requests"].update({ set("status", "rejected") }) {
                filter { eq("id", requestId) }
            }
            Unit
        }

    override suspend fun getPartners(userId: String): Result<List<Partner>> =
        runCatching {
            db["partners"].select { filter { eq("user_id", userId) } }
                .decodeList<PartnerDto>().map { it.toDomain() }
        }

    override suspend fun getPendingRequests(userId: String): Result<List<PartnerRequest>> =
        runCatching {
            db["partner_requests"].select {
                filter { eq("to_user_id", userId); eq("status", "pending") }
            }.decodeList<PartnerRequestDto>().map { it.toDomain() }
        }

    override suspend fun getSentRequests(userId: String): Result<List<PartnerRequest>> =
        runCatching {
            db["partner_requests"].select {
                filter { eq("from_user_id", userId); eq("status", "pending") }
            }.decodeList<PartnerRequestDto>().map { it.toDomain() }
        }

    override suspend fun removePartner(partnerId: String): Result<Unit> = runCatching {
        val uid = currentUserId ?: error("Not authenticated")
        db["partners"].delete {
            filter { eq("user_id", uid); eq("partner_id", partnerId) }
        }
        Unit
    }

    override suspend fun isPartner(userId: String, targetId: String): Result<Boolean> =
        runCatching {
            val result = db["partners"].select {
                filter { eq("user_id", userId); eq("partner_id", targetId) }
            }.decodeSingleOrNull<PartnerDto>()
            result != null
        }

    override fun observePartners(userId: String): Flow<List<Partner>> = flow {
        emit(getPartners(userId).getOrDefault(emptyList()))
    }

    override fun observePendingRequests(userId: String): Flow<List<PartnerRequest>> = flow {
        emit(getPendingRequests(userId).getOrDefault(emptyList()))
    }
}
