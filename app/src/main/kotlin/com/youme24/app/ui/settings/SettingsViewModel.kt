package com.youme24.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.data.local.DataStoreManager
import com.youme24.app.domain.model.User
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentUser: User? = null,
    val isDarkMode: Boolean = false,
    val aiEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isLoggedOut: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepo: IAuthRepository,
    private val userRepo: IUserRepository,
    private val dataStore: DataStoreManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepo.currentUserId ?: return@launch
            val user = userRepo.getUserById(uid).getOrNull()
            _uiState.update { it.copy(currentUser = user) }
        }
        dataStore.isDarkMode
            .onEach { v -> _uiState.update { it.copy(isDarkMode = v) } }
            .launchIn(viewModelScope)
        dataStore.isAiEnabled
            .onEach { v -> _uiState.update { it.copy(aiEnabled = v) } }
            .launchIn(viewModelScope)
        dataStore.notificationsEnabled
            .onEach { v -> _uiState.update { it.copy(notificationsEnabled = v) } }
            .launchIn(viewModelScope)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDarkMode(enabled) }
    }
    fun setAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAiEnabled(enabled)
            val uid = authRepo.currentUserId ?: return@launch
            userRepo.updateAiEnabled(uid, enabled)
        }
    }
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.setNotificationsEnabled(enabled) }
    }
    fun logout() {
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
    fun deleteAccount() {
        viewModelScope.launch {
            authRepo.deleteAccount()
                .onSuccess { _uiState.update { it.copy(isLoggedOut = true) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
