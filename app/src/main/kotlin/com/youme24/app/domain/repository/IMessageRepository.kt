package com.youme24.app.domain.repository

import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.Conversation
import com.youme24.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface domaine — messages chiffrés, statuts, abonnements temps réel.
 * Équivalent de src/domain/repositories/IMessageRepository.ts
 */
interface IMessageRepository {
    // Conversations
    suspend fun getConversations(userId: String): Result<List<Conversation>>
    suspend fun getOrCreateConversation(userId: String, partnerId: String): Result<Conversation>
    fun observeConversations(userId: String): Flow<List<Conversation>>

    // Messages
    suspend fun sendMessage(message: Message): Result<Message>
    suspend fun getMessageById(id: String): Result<Message?>
    suspend fun getConversationMessages(
        conversationId: String,
        limit: Int = 50,
        before: Long? = null,
    ): Result<List<Message>>
    suspend fun updateMessageStatus(messageId: String, status: com.youme24.app.domain.model.MessageStatus): Result<Unit>
    suspend fun updateMessageAiAnalysis(messageId: String, analysis: AiAnalysis): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun searchMessages(conversationId: String, query: String): Result<List<Message>>
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit>
    fun observeMessages(conversationId: String): Flow<List<Message>>
}
