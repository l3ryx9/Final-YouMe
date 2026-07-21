package com.youme24.app.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.MessageLocation
import com.youme24.app.domain.repository.ILocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveLocationUiState(
    val isSharing: Boolean = false,
    val myLocation: MessageLocation? = null,
    val partnerLocation: MessageLocation? = null,
    val stealthActive: Boolean = false,
    val tapCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class LiveLocationViewModel @Inject constructor(
    private val locationRepo: ILocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveLocationUiState())
    val uiState: StateFlow<LiveLocationUiState> = _uiState.asStateFlow()

    private var conversationId: String = ""

    fun init(conversationId: String) {
        this.conversationId = conversationId
        locationRepo.observeCurrentLocation()
            .onEach { loc -> _uiState.update { it.copy(myLocation = loc) } }
            .launchIn(viewModelScope)
        locationRepo.observePartnerLocation(conversationId)
            .onEach { loc -> _uiState.update { it.copy(partnerLocation = loc) } }
            .launchIn(viewModelScope)
    }

    fun startSharing() {
        viewModelScope.launch {
            locationRepo.startSharing(conversationId)
            _uiState.update { it.copy(isSharing = true) }
        }
    }

    fun stopSharing() {
        viewModelScope.launch {
            locationRepo.stopSharing(conversationId)
            _uiState.update { it.copy(isSharing = false) }
        }
    }

    /**
     * Mode furtif — activation par 5 taps rapides (reproduit le locationStore.tapCount).
     * En production, le toggle est caché dans l'UI.
     */
    fun toggleStealthMode() {
        val current = _uiState.value
        val newCount = current.tapCount + 1
        if (newCount >= 5) {
            viewModelScope.launch {
                if (!current.stealthActive) {
                    locationRepo.enableStealthMode(conversationId)
                } else {
                    locationRepo.disableStealthMode()
                }
            }
            _uiState.update { it.copy(stealthActive = !it.stealthActive, tapCount = 0) }
        } else {
            _uiState.update { it.copy(tapCount = newCount) }
        }
    }
}
