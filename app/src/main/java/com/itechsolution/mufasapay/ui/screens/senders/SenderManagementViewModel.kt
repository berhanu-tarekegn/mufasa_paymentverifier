package com.itechsolution.mufasapay.ui.screens.senders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.Sender
import com.itechsolution.mufasapay.domain.model.SenderTemplate
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.usecase.sender.AddSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.GetAllSendersUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.RemoveSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.ToggleSenderStatusUseCase
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    private val selectedSenderId = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSenderTemplates: StateFlow<List<SenderTemplate>> = selectedSenderId
        .flatMapLatest { senderId ->
            if (senderId == null) {
                flowOf(emptyList())
            } else {
                senderRepository.getTemplatesForSenderFlow(senderId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Toggle sender enabled/disabled status
     */
    fun toggleSenderStatus(senderId: String) {
        viewModelScope.launch {
            val currentSender = senders.value?.find { it.senderId == senderId }
            if (currentSender == null) {
                _errorMessage.value = "Sender not found"
                return@launch
            }

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
    fun addSender(senderId: String, displayName: String) {
        viewModelScope.launch {
            if (senderId.isBlank() || displayName.isBlank()) {
                _errorMessage.value = "Sender ID and Display Name are required"
                return@launch
            }

            when (val result = addSenderUseCase(senderId, displayName)) {
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
     * Load templates for a specific sender
     */
    fun loadTemplatesForSender(senderId: String) {
        selectedSenderId.value = senderId
    }

    fun clearSelectedSender() {
        selectedSenderId.value = null
    }

    /**
     * Add a new template to a sender
     */
    fun addTemplate(senderId: String, label: String, pattern: String) {
        viewModelScope.launch {
            if (label.isBlank()) {
                _errorMessage.value = "Template label is required"
                return@launch
            }
            if (pattern.isBlank()) {
                _errorMessage.value = "Template pattern is required"
                return@launch
            }
            validateTemplatePattern(pattern)?.let { validationError ->
                _errorMessage.value = validationError
                return@launch
            }

            val template = SenderTemplate(
                senderId = senderId,
                label = label.trim(),
                pattern = pattern.trim(),
                isEnabled = true,
                createdAt = DateTimeUtils.getCurrentTimestamp()
            )

            when (val result = senderRepository.addTemplate(template)) {
                is Result.Success -> {
                    _successMessage.value = "Template '$label' added successfully"
                }
                is Result.Error -> {
                    _errorMessage.value = result.message ?: "Failed to add template"
                }
                else -> {}
            }
        }
    }

    fun updateTemplate(templateId: Long, senderId: String, label: String, pattern: String, isEnabled: Boolean) {
        viewModelScope.launch {
            if (label.isBlank()) {
                _errorMessage.value = "Template label is required"
                return@launch
            }
            if (pattern.isBlank()) {
                _errorMessage.value = "Template pattern is required"
                return@launch
            }
            validateTemplatePattern(pattern)?.let { validationError ->
                _errorMessage.value = validationError
                return@launch
            }

            val existingTemplate = selectedSenderTemplates.value.find { it.id == templateId }
            val template = SenderTemplate(
                id = templateId,
                senderId = senderId,
                label = label.trim(),
                pattern = pattern.trim(),
                isEnabled = isEnabled,
                createdAt = existingTemplate?.createdAt ?: DateTimeUtils.getCurrentTimestamp()
            )

            when (val result = senderRepository.updateTemplate(template)) {
                is Result.Success -> {
                    _successMessage.value = "Template '$label' updated successfully"
                }
                is Result.Error -> {
                    _errorMessage.value = result.message ?: "Failed to update template"
                }
                else -> {}
            }
        }
    }

    fun toggleTemplateEnabled(template: SenderTemplate) {
        viewModelScope.launch {
            when (val result = senderRepository.updateTemplate(template.copy(isEnabled = !template.isEnabled))) {
                is Result.Success -> {
                    val status = if (template.isEnabled) "disabled" else "enabled"
                    _successMessage.value = "Template '${template.label}' $status"
                }
                is Result.Error -> {
                    _errorMessage.value = result.message ?: "Failed to update template"
                }
                else -> {}
            }
        }
    }

    /**
     * Remove a template
     */
    fun removeTemplate(templateId: Long) {
        viewModelScope.launch {
            when (val result = senderRepository.removeTemplate(templateId)) {
                is Result.Success -> {
                    _successMessage.value = "Template removed"
                }
                is Result.Error -> {
                    _errorMessage.value = result.message ?: "Failed to remove template"
                }
                else -> {}
            }
        }
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun validateTemplatePattern(pattern: String): String? {
        return when {
            !pattern.contains("{amount}") -> "Template must include {amount} to extract sums"
            !pattern.contains("{transaction}") -> "Template must include {transaction} to build the webhook payload"
            else -> null
        }
    }
}
