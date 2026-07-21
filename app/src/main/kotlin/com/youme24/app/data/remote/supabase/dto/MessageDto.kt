package com.youme24.app.data.remote.supabase.dto

import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.Message
import com.youme24.app.domain.model.MessageLocation
import com.youme24.app.domain.model.MessageStatus
import com.youme24.app.domain.model.MessageType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")                val id: String,
    @SerialName("conversation_id")   val conversationId: String,
    @SerialName("sender_id")         val senderId: String,
    @SerialName("receiver_id")       val receiverId: String,
    @SerialName("type")              val type: String,
    @SerialName("content")           val content: String? = null,
    @SerialName("encrypted")         val encrypted: Boolean = false,
    @SerialName("nonce")             val nonce: String? = null,
    @SerialName("voice_local_path")  val voiceLocalPath: String? = null,
    @SerialName("voice_duration")    val voiceDuration: Int? = null,
    @SerialName("image_local_path")  val imageLocalPath: String? = null,
    @SerialName("video_local_path")  val videoLocalPath: String? = null,
    @SerialName("storage_url")       val storageUrl: String? = null,
    @SerialName("latitude")          val latitude: Double? = null,
    @SerialName("longitude")         val longitude: Double? = null,
    @SerialName("accuracy")          val accuracy: Float? = null,
    @SerialName("status")            val status: String = "sent",
    @SerialName("is_deleted")        val isDeleted: Boolean = false,
    @SerialName("created_at")        val createdAt: Long = 0L,
    @SerialName("updated_at")        val updatedAt: Long = 0L,
) {
    fun toDomain() = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        receiverId = receiverId,
        type = MessageType.valueOf(type.uppercase()),
        content = content,
        encrypted = encrypted,
        nonce = nonce,
        voiceLocalPath = voiceLocalPath,
        voiceDuration = voiceDuration,
        imageLocalPath = imageLocalPath,
        videoLocalPath = videoLocalPath,
        storageUrl = storageUrl,
        location = if (latitude != null && longitude != null)
            MessageLocation(latitude, longitude, accuracy) else null,
        status = runCatching { MessageStatus.valueOf(status.uppercase()) }
            .getOrDefault(MessageStatus.SENT),
        isDeleted = isDeleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
