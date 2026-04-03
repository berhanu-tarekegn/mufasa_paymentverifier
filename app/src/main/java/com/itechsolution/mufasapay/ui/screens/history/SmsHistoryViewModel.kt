package com.itechsolution.mufasapay.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.SmsMessage
import com.itechsolution.mufasapay.domain.usecase.history.DeleteSmsTransactionUseCase
import com.itechsolution.mufasapay.domain.usecase.history.GetSmsHistoryUseCase
import com.itechsolution.mufasapay.domain.usecase.history.RetryFailedDeliveryUseCase
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Filter type for SMS history
 */
enum class FilterType {
    ALL,
    FORWARDED,
    NEEDS_ACTION
}

data class TransactionSummary(
    val total: Int = 0,
    val forwarded: Int = 0,
    val needsAction: Int = 0
)

/**
 * ViewModel for SMS history screen
 */
class SmsHistoryViewModel(
    private val getSmsHistoryUseCase: GetSmsHistoryUseCase,
    private val retryFailedDeliveryUseCase: RetryFailedDeliveryUseCase,
    private val deleteSmsTransactionUseCase: DeleteSmsTransactionUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(FilterType.ALL)
    val filter: StateFlow<FilterType> = _filter.asStateFlow()

    private val _allMessages = MutableStateFlow<List<SmsMessage>>(emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Combine messages and filter to get filtered list
    val messages: StateFlow<List<SmsMessage>> = combine(_allMessages, _filter) { messages, filter ->
        when (filter) {
            FilterType.ALL -> messages
            FilterType.FORWARDED -> messages.filter { it.isForwarded }
            FilterType.NEEDS_ACTION -> messages.filter { !it.isForwarded }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val summary: StateFlow<TransactionSummary> = _allMessages
        .combine(_filter) { messages, _ ->
            TransactionSummary(
                total = messages.size,
                forwarded = messages.count { it.isForwarded },
                needsAction = messages.count { !it.isForwarded }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionSummary()
        )

    init {
        loadMessages()
    }

    /**
     * Load SMS messages from database
     */
    private fun loadMessages() {
        viewModelScope.launch {
            getSmsHistoryUseCase().collect { messages ->
                _allMessages.value = messages
            }
        }
    }

    /**
     * Set filter for messages
     */
    fun setFilter(filter: FilterType) {
        _filter.value = filter
    }

    /**
     * Retry failed delivery for a specific SMS
     */
    fun retryDelivery(smsId: Long) {
        viewModelScope.launch {
            when (val result = retryFailedDeliveryUseCase(smsId)) {
                is Result.Success -> {
                    Timber.d("Retry delivery initiated for SMS: $smsId")
                    _successMessage.value = "Retry initiated. The SMS will be forwarded again."
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to retry delivery"
                    Timber.e(result.exception, message)
                    _errorMessage.value = message
                }

                else -> {}
            }
        }
    }

    fun deleteMessage(smsId: Long) {
        viewModelScope.launch {
            when (val result = deleteSmsTransactionUseCase(smsId)) {
                is Result.Success -> {
                    _successMessage.value = "Transaction deleted successfully"
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to delete transaction"
                    Timber.e(result.exception, message)
                    _errorMessage.value = message
                }
                else -> {}
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
