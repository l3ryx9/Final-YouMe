package com.youme24.app.data.remote.supabase.dto

import com.youme24.app.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO Supabase — table "users".
 * Mapping DTO ↔ domaine isolé ici pour ne pas polluer le domaine.
 */
@Serializable
data class UserDto(
    @SerialName("id")              val id: String,
    @SerialName("email")           val email: String,
    @SerialName("username")        val username: String,
    @SerialName("display_name")    val displayName: String,
    @SerialName("photo_url")       val photoUrl: String? = null,
    @SerialName("bio")             val bio: String? = null,
    @SerialName("is_online")       val isOnline: Boolean = false,
    @SerialName("last_seen")       val lastSeen: Long? = null,
    @SerialName("created_at")      val createdAt: Long = 0L,
    @SerialName("updated_at")      val updatedAt: Long = 0L,
    @SerialName("is_email_verified") val isEmailVerified: Boolean = false,
    @SerialName("ai_enabled")      val aiEnabled: Boolean = true,
    @SerialName("fcm_token")       val fcmToken: String? = null,
    @SerialName("e2e_public_key")  val e2ePublicKey: String? = null,
) {
    fun toDomain() = User(
        id = id, email = email, username = username,
        displayName = displayName, photoUrl = photoUrl, bio = bio,
        isOnline = isOnline, lastSeen = lastSeen, createdAt = createdAt,
        updatedAt = updatedAt, isEmailVerified = isEmailVerified,
        aiEnabled = aiEnabled, fcmToken = fcmToken, e2ePublicKey = e2ePublicKey,
    )
}

fun User.toDto() = UserDto(
    id = id, email = email, username = username,
    displayName = displayName, photoUrl = photoUrl, bio = bio,
    isOnline = isOnline, lastSeen = lastSeen, createdAt = createdAt,
    updatedAt = updatedAt, isEmailVerified = isEmailVerified,
    aiEnabled = aiEnabled, fcmToken = fcmToken, e2ePublicKey = e2ePublicKey,
)
