package com.youme24.app.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youme24.app.domain.model.DailyAnalysisReport
import com.youme24.app.domain.repository.IGeminiRepository
import com.youme24.app.domain.repository.IMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnalysisUiState(
    val report: DailyAnalysisReport? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val memoryRepo: IMemoryRepository,
    private val geminiRepo: IGeminiRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun init(conversationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val today = LocalDate.now().toString()

            // Try to get existing report
            val existing = memoryRepo.getDailyAnalysis(conversationId, today).getOrNull()
            if (existing != null) {
                _uiState.update { it.copy(report = existing, isLoading = false) }
                return@launch
            }

            // Trigger daily analysis Edge Function
            geminiRepo.triggerDailyAnalysis(conversationId, today)

            // Poll for result (simplified — in production use Realtime)
            val report = memoryRepo.getDailyAnalysis(conversationId, today).getOrNull()
            _uiState.update { it.copy(report = report, isLoading = false) }
        }
    }
}
