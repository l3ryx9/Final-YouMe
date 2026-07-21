package com.youme24.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Domaine : entité Conversation.
 * Équivalent de src/domain/Conversation.ts (React Native).
 */
@Serializable
data class Conversation(
    val id: String,
    val participantIds: List<String>,     // exactement 2 IDs
    val lastMessage: LastMessagePreview? = null,
    val unreadCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@Serializable
data class LastMessagePreview(
    val id: String,
    val senderId: String,
    val type: MessageType,
    val content: String?,
    val createdAt: Long,
)
