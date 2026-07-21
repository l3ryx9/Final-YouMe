package com.youme24.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.User
import com.youme24.app.domain.repository.IPartnerRepository
import com.youme24.app.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val results: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val sendingTo: String? = null,
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepo: IUserRepository,
    private val partnerRepo: IPartnerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .debounce(400)
            .filter { it.length >= 2 }
            .distinctUntilChanged()
            .onEach { q ->
                _uiState.update { it.copy(isLoading = true) }
                userRepo.searchUsersByUsername(q)
                    .onSuccess { list -> _uiState.update { it.copy(results = list, isLoading = false) } }
                    .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            }
            .launchIn(viewModelScope)
    }

    fun search(query: String) { queryFlow.value = query }

    fun sendRequest(toUserId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(sendingTo = toUserId) }
            partnerRepo.sendPartnerRequest(toUserId)
            _uiState.update { it.copy(sendingTo = null) }
        }
    }
}
