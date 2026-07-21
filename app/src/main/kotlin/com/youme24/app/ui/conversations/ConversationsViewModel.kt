package com.youme24.app.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.Conversation
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val messageRepo: IMessageRepository,
    private val authRepo: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init { loadConversations() }

    fun loadConversations() {
        val uid = authRepo.currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            messageRepo.getConversations(uid)
                .onSuccess { convs -> _uiState.update { it.copy(conversations = convs, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
        messageRepo.observeConversations(uid)
            .onEach { convs -> _uiState.update { it.copy(conversations = convs) } }
            .launchIn(viewModelScope)
    }
}
