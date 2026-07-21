package com.youme24.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.data.crypto.E2ECryptoService
import com.youme24.app.data.crypto.KeyStorage
import com.youme24.app.domain.model.AiAnalysis
import com.youme24.app.domain.model.Message
import com.youme24.app.domain.model.MessageStatus
import com.youme24.app.domain.model.MessageType
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IGeminiRepository
import com.youme24.app.domain.repository.IMessageRepository
import com.youme24.app.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val coherenceScore: Float? = null,
    val isAiEnabled: Boolean = true,
    val partnerIsOnline: Boolean = false,
    val partnerDisplayName: String = "",
    val partnerPhotoUrl: String? = null,
)

/**
 * ViewModel chat — remplace conversationStore.ts + ChatViewModel RN.
 * StateFlow, coroutines, Flow temps réel depuis Supabase Realtime.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepo: IMessageRepository,
    private val userRepo: IUserRepository,
    private val authRepo: IAuthRepository,
    private val geminiRepo: IGeminiRepository,
    private val cryptoService: E2ECryptoService,
    private val keyStorage: KeyStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: String = ""
    private var partnerId: String = ""
    private val currentUserId get() = authRepo.currentUserId ?: ""

    fun init(conversationId: String) {
        this.conversationId = conversationId
        loadMessages()
        observeMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            messageRepo.getConversationMessages(conversationId)
                .onSuccess { msgs ->
                    _uiState.update { it.copy(messages = msgs, isLoading = false) }
                    messageRepo.markMessagesAsRead(conversationId, currentUserId)
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    private fun observeMessages() {
        messageRepo.observeMessages(conversationId)
            .onEach { msgs -> _uiState.update { it.copy(messages = msgs) } }
            .launchIn(viewModelScope)
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            // E2E encryption
            val partnerKey = keyStorage.getPartnerPublicKey(partnerId)
            val (content, nonce, encrypted) = if (partnerKey != null) {
                val sharedKey = cryptoService.deriveSharedSecret(partnerKey)
                val (cipher, iv) = cryptoService.encrypt(text, sharedKey)
                Triple(cipher, iv, true)
            } else {
                Triple(text, null, false)
            }

            val message = Message(
                id             = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId       = currentUserId,
                receiverId     = partnerId,
                type           = MessageType.TEXT,
                content        = content,
                encrypted      = encrypted,
                nonce          = nonce,
                status         = MessageStatus.SENDING,
                createdAt      = System.currentTimeMillis(),
            )

            messageRepo.sendMessage(message)
                .onSuccess { _uiState.update { it.copy(isSending = false) } }
                .onFailure { e -> _uiState.update { it.copy(isSending = false, error = e.message) } }

            // Async AI analysis
            if (_uiState.value.isAiEnabled) analyzeLastMessages()
        }
    }

    fun sendLocationMessage(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = currentUserId,
                receiverId = partnerId,
                type = MessageType.LOCATION,
                location = com.youme24.app.domain.model.MessageLocation(latitude, longitude),
                status = MessageStatus.SENDING,
                createdAt = System.currentTimeMillis(),
            )
            messageRepo.sendMessage(message)
        }
    }

    fun sendVoiceMessage(localPath: String, durationSecs: Int) {
        viewModelScope.launch {
            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = currentUserId,
                receiverId = partnerId,
                type = MessageType.VOICE,
                voiceLocalPath = localPath,
                voiceDuration = durationSecs,
                status = MessageStatus.SENDING,
                createdAt = System.currentTimeMillis(),
            )
            messageRepo.sendMessage(message)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch { messageRepo.deleteMessage(messageId) }
    }

    fun addReaction(messageId: String, emoji: String) {
        // Update via Supabase JSONB column reactions
        viewModelScope.launch {
            // Optimistic UI update
            _uiState.update { state ->
                state.copy(messages = state.messages.map { msg ->
                    if (msg.id == messageId)
                        msg.copy(reactions = msg.reactions + (currentUserId to emoji))
                    else msg
                })
            }
        }
    }

    private fun analyzeLastMessages() {
        viewModelScope.launch {
            val recent = _uiState.value.messages.takeLast(10).mapNotNull { it.content }
            geminiRepo.getCoherenceScore(conversationId, recent)
                .onSuccess { score -> _uiState.update { it.copy(coherenceScore = score) } }
        }
    }

    fun askGemini(prompt: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val context = _uiState.value.messages.takeLast(5).mapNotNull { it.content }
            geminiRepo.sendPrompt(prompt, context.joinToString("\n"))
                .onSuccess { onResult(it) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
