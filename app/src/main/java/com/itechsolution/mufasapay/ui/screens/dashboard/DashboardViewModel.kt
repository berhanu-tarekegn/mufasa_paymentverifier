package com.itechsolution.mufasapay.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.usecase.dashboard.DeliveryStats
import com.itechsolution.mufasapay.domain.usecase.dashboard.GetDeliveryStatsUseCase
import com.itechsolution.mufasapay.domain.usecase.history.GetSmsHistoryUseCase
import com.itechsolution.mufasapay.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for dashboard screen
 */
class DashboardViewModel(
    getDeliveryStatsUseCase: GetDeliveryStatsUseCase,
    private val getSmsHistoryUseCase: GetSmsHistoryUseCase
) : ViewModel() {

    val statsState: StateFlow<UiState<DeliveryStats>> = getDeliveryStatsUseCase()
        .map<DeliveryStats, UiState<DeliveryStats>> { UiState.Success(it) }
        .onStart { emit(UiState.Loading) }
        .catch { e ->
            Timber.e(e, "Error loading delivery stats")
            emit(UiState.Error(e.message ?: "Failed to load statistics", e as? Exception))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _recentMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val recentMessages: StateFlow<List<SmsMessage>> = _recentMessages

    init {
        loadRecentMessages()
    }

    /**
     * Load recent SMS messages (last 10)
     */
    private fun loadRecentMessages() {
        viewModelScope.launch {
            getSmsHistoryUseCase().collect { messages ->
                _recentMessages.value = messages.take(10)
            }
        }
    }
}
