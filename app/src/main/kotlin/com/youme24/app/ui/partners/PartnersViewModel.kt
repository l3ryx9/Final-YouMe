package com.youme24.app.ui.partners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.Partner
import com.youme24.app.domain.model.PartnerRequest
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IPartnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartnersUiState(
    val partners: List<Partner> = emptyList(),
    val pendingRequests: List<PartnerRequest> = emptyList(),
    val sentRequests: List<PartnerRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class PartnersViewModel @Inject constructor(
    private val partnerRepo: IPartnerRepository,
    private val authRepo: IAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartnersUiState())
    val uiState: StateFlow<PartnersUiState> = _uiState.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        val uid = authRepo.currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val partners = partnerRepo.getPartners(uid).getOrDefault(emptyList())
            val pending  = partnerRepo.getPendingRequests(uid).getOrDefault(emptyList())
            val sent     = partnerRepo.getSentRequests(uid).getOrDefault(emptyList())
            _uiState.update { it.copy(partners = partners, pendingRequests = pending, sentRequests = sent, isLoading = false) }
        }
        partnerRepo.observePartners(uid).onEach { list ->
            _uiState.update { it.copy(partners = list) }
        }.launchIn(viewModelScope)
        partnerRepo.observePendingRequests(uid).onEach { list ->
            _uiState.update { it.copy(pendingRequests = list) }
        }.launchIn(viewModelScope)
    }

    fun sendRequest(toUserId: String) {
        viewModelScope.launch {
            partnerRepo.sendPartnerRequest(toUserId)
                .onSuccess { _uiState.update { it.copy(successMessage = "Demande envoyée !") } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            partnerRepo.acceptPartnerRequest(requestId)
                .onSuccess { _uiState.update { it.copy(successMessage = "Partenaire ajouté !") } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            loadAll()
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            partnerRepo.rejectPartnerRequest(requestId)
            loadAll()
        }
    }

    fun removePartner(partnerId: String) {
        viewModelScope.launch {
            partnerRepo.removePartner(partnerId)
            loadAll()
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}
