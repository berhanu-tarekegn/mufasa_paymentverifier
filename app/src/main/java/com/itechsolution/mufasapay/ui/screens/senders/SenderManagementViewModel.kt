package com.itechsolution.mufasapay.ui.screens.senders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.usecase.sender.AddSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.GetAllSendersUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.RemoveSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.ToggleSenderStatusUseCase
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for sender management screen
 */
class SenderManagementViewModel(
    private val getAllSendersUseCase: GetAllSendersUseCase,
    private val toggleSenderStatusUseCase: ToggleSenderStatusUseCase,
    private val addSenderUseCase: AddSenderUseCase,
    private val removeSenderUseCase: RemoveSenderUseCase,
    private val senderRepository: SenderRepository
) : ViewModel() {

    val senders: StateFlow<List<Sender>?> = getAllSendersUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Toggle sender enabled/disabled status
     */
    fun toggleSenderStatus(senderId: String) {
        viewModelScope.launch {
            // Find current sender to get current status
            val currentSender = senders.value?.find { it.senderId == senderId }
            if (currentSender == null) {
                _errorMessage.value = "Sender not found"
                return@launch
            }

            // Toggle the status
            val newStatus = !currentSender.isEnabled

            when (val result = toggleSenderStatusUseCase(senderId, newStatus)) {
                is Result.Success -> {
                    Timber.d("Toggled sender status: $senderId -> $newStatus")
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to toggle sender status"
                    Timber.e(result.exception, message)
                    _errorMessage.value = message
                }

                else -> {}
            }
        }
    }

    /**
     * Add a new sender
     */
    fun addSender(senderId: String, displayName: String, pattern: String?) {
        viewModelScope.launch {
            if (senderId.isBlank() || displayName.isBlank()) {
                _errorMessage.value = "Sender ID and Display Name are required"
                return@launch
            }

            when (val result = addSenderUseCase(senderId, displayName, pattern)) {
                is Result.Success -> {
                    Timber.d("Added sender: $senderId")
                    _successMessage.value = "Sender added successfully"
                    _showAddDialog.value = false
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to add sender"
                    Timber.e(result.exception, message)
                    _errorMessage.value = message
                }

                else -> {}
            }
        }
    }

    /**
     * Remove a sender
     */
    fun removeSender(senderId: String) {
        viewModelScope.launch {
            when (val result = removeSenderUseCase(senderId)) {
                is Result.Success -> {
                    Timber.d("Removed sender: $senderId")
                    _successMessage.value = "Sender removed successfully"
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to remove sender"
                    Timber.e(result.exception, message)
                    _errorMessage.value = message
                }

                else -> {}
            }
        }
    }

    /**
     * Update a sender's pattern
     */
    fun updateSenderPattern(senderId: String, pattern: String) {
        viewModelScope.launch {
            val sender = senders.value?.find { it.senderId == senderId }
            if (sender == null) {
                _errorMessage.value = "Sender not found"
                return@launch
            }

            val updatedSender = sender.copy(pattern = pattern)
            when (val result = senderRepository.updateSender(updatedSender)) {
                is Result.Success -> {
                    _successMessage.value = "Pattern saved successfully"
                }
                is Result.Error -> {
                    _errorMessage.value = result.message ?: "Failed to save pattern"
                }
                else -> {}
            }
        }
    }

    /**
     * Show add sender dialog
     */
    fun showAddDialog() {
        _showAddDialog.value = true
    }

    /**
     * Hide add sender dialog
     */
    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
