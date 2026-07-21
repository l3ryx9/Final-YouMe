package com.youme24.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.User
import com.youme24.app.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────
//  State
// ─────────────────────────────────────────────────────────────

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

/**
 * ViewModel auth — remplace authStore.ts (Zustand).
 * StateFlow exposé aux écrans Compose.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        authRepo.observeAuthState()
            .onEach { user ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = user != null,
                        currentUser = user,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepo.signIn(email, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = user, isAuthenticated = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun register(email: String, password: String, username: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepo.signUp(email, password, username, displayName)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = user, isAuthenticated = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepo.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Email de réinitialisation envoyé !"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
}
