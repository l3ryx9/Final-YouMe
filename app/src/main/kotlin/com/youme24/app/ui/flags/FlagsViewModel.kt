package com.youme24.app.ui.flags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.RelationalFlag
import com.youme24.app.domain.repository.IMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlagsUiState(
    val flags: List<RelationalFlag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FlagsViewModel @Inject constructor(
    private val memoryRepo: IMemoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlagsUiState())
    val uiState: StateFlow<FlagsUiState> = _uiState.asStateFlow()

    fun init(partnerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Fetch flags for this partner across all conversations
            memoryRepo.getFlags(partnerId)
                .onSuccess { list -> _uiState.update { it.copy(flags = list, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
