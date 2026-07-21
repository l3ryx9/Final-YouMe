package com.youme24.app.data.remote.supabase

import com.youme24.app.data.remote.supabase.dto.MessageDto
import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.Conversation
import com.youme24.app.domain.model.Message
import com.youme24.app.domain.model.MessageStatus
import com.youme24.app.domain.repository.IMessageRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider,
) : IMessageRepository {

    private val db get() = supabase.client.postgrest
    private val channels = mutableMapOf<String, RealtimeChannel>()

    override suspend fun getConversations(userId: String): Result<List<Conversation>> =
        runCatching {
            db["conversations"].select {
                filter { contains("participant_ids", listOf(userId)) }
                order("updated_at", Order.DESCENDING)
            }.decodeList<ConversationDto>().map { it.toDomain() }
        }

    override suspend fun getOrCreateConversation(
        userId: String,
        partnerId: String,
    ): Result<Conversation> = runCatching {
        // Try to find existing
        val existing = db["conversations"].select {
            filter { contains("participant_ids", listOf(userId, partnerId)) }
        }.decodeSingleOrNull<ConversationDto>()

        if (existing != null) return@runCatching existing.toDomain()

        // Create new
        db["conversations"].insert(
            mapOf("participant_ids" to listOf(userId, partnerId))
        ).decodeSingle<ConversationDto>().toDomain()
    }

    override fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        emit(getConversations(userId).getOrDefault(emptyList()))
        // TODO: Wire Realtime channel for live updates
    }

    override suspend fun sendMessage(message: Message): Result<Message> = runCatching {
        val dto = MessageDto(
            id = message.id, conversationId = message.conversationId,
            senderId = message.senderId, receiverId = message.receiverId,
            type = message.type.name.lowercase(),
            content = message.content, encrypted = message.encrypted,
            nonce = message.nonce, storageUrl = message.storageUrl,
            latitude = message.location?.latitude,
            longitude = message.location?.longitude,
            accuracy = message.location?.accuracy,
            status = message.status.name.lowercase(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        db["messages"].insert(dto).decodeSingle<MessageDto>().toDomain()
    }

    override suspend fun getMessageById(id: String): Result<Message?> = runCatching {
        db["messages"].select { filter { eq("id", id) } }
            .decodeSingleOrNull<MessageDto>()?.toDomain()
    }

    override suspend fun getConversationMessages(
        conversationId: String,
        limit: Int,
        before: Long?,
    ): Result<List<Message>> = runCatching {
        db["messages"].select {
            filter {
                eq("conversation_id", conversationId)
                eq("is_deleted", false)
                before?.let { lte("created_at", it) }
            }
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<MessageDto>().map { it.toDomain() }.reversed()
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus,
    ): Result<Unit> = runCatching {
        db["messages"].update({ set("status", status.name.lowercase()) }) {
            filter { eq("id", messageId) }
        }
    }

    override suspend fun updateMessageAiAnalysis(
        messageId: String,
        analysis: AiAnalysis,
    ): Result<Unit> = runCatching {
        db["messages"].update({
            set("ai_analysis", analysis)
        }) { filter { eq("id", messageId) } }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> = runCatching {
        db["messages"].update({ set("is_deleted", true) }) {
            filter { eq("id", messageId) }
        }
    }

    override suspend fun searchMessages(
        conversationId: String,
        query: String,
    ): Result<List<Message>> = runCatching {
        db["messages"].select {
            filter {
                eq("conversation_id", conversationId)
                ilike("content", "%$query%")
            }
        }.decodeList<MessageDto>().map { it.toDomain() }
    }

    override suspend fun markMessagesAsRead(
        conversationId: String,
        userId: String,
    ): Result<Unit> = runCatching {
        db["messages"].update({ set("status", "read") }) {
            filter {
                eq("conversation_id", conversationId)
                neq("sender_id", userId)
                neq("status", "read")
            }
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        emit(getConversationMessages(conversationId).getOrDefault(emptyList()))
        // Realtime: subscribe to messages table for this conversation
    }
}

// Minimal DTO for Conversation (same pattern as MessageDto)
@Serializable
private data class ConversationDto(
    @SerialName("id")               val id: String,
    @SerialName("participant_ids")  val participantIds: List<String>,
    @SerialName("unread_count")     val unreadCount: Int = 0,
    @SerialName("created_at")       val createdAt: Long = 0L,
    @SerialName("updated_at")       val updatedAt: Long = 0L,
) {
    fun toDomain() = Conversation(
        id = id, participantIds = participantIds,
        unreadCount = unreadCount, createdAt = createdAt, updatedAt = updatedAt,
    )
}
