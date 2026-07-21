package com.youme24.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domaine : entité Message (chat 1-to-1).
 * Équivalent de src/domain/Message.ts (React Native).
 */
@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: MessageType,
    val content: String? = null,         // texte clair ou null si chiffré
    val encrypted: Boolean = false,
    val nonce: String? = null,           // Base64 — XSalsa20-Poly1305
    // Audio
    val voiceLocalPath: String? = null,
    val voiceDuration: Int? = null,      // secondes
    val voiceTranscription: String? = null,
    // Media
    val imageLocalPath: String? = null,
    val videoLocalPath: String? = null,
    val storageUrl: String? = null,
    // Location
    val location: MessageLocation? = null,
    // State
    val status: MessageStatus = MessageStatus.SENDING,
    val aiAnalysis: AiAnalysis? = null,
    val reactions: Map<String, String> = emptyMap(), // userId → emoji
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

enum class MessageType { TEXT, VOICE, SYSTEM, LOCATION, IMAGE, VIDEO }

enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }

@Serializable
data class MessageLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val speed: Float? = null,
    val isMocked: Boolean = false,
)

@Serializable
data class AiAnalysis(
    val emotions: List<String> = emptyList(),
    val summary: String? = null,
    val topics: List<String> = emptyList(),
    val entities: List<String> = emptyList(),
    val transcription: String? = null,
    val coherenceScore: Float? = null,
)
