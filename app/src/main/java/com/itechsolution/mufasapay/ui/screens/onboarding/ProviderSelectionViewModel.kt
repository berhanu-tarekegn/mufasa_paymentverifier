package com.itechsolution.mufasapay.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.SenderTemplate
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.usecase.sender.AddSenderUseCase
import com.itechsolution.mufasapay.util.DateTimeUtils
import com.itechsolution.mufasapay.ui.state.ProviderItem
import com.itechsolution.mufasapay.ui.util.EthiopianProviders
import com.itechsolution.mufasapay.ui.util.PreferencesManager
import com.itechsolution.mufasapay.ui.util.ProviderTemplateDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for provider selection onboarding screen
 */
class ProviderSelectionViewModel(
    private val addSenderUseCase: AddSenderUseCase,
    private val senderRepository: SenderRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _providers = MutableStateFlow(EthiopianProviders.ALL)
    val providers: StateFlow<List<ProviderItem>> = _providers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Toggle provider selection
     */
    fun toggleProvider(senderId: String) {
        _providers.value = _providers.value.map { provider ->
            if (provider.senderId == senderId) {
                Timber.d("Toggled provider: $senderId")
                provider.copy(isSelected = !provider.isSelected)
            } else {
                provider
            }
        }
    }

    /**
     * Save selected providers and mark onboarding complete
     */
    fun saveSelections() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val selectedProviders = _providers.value.filter { it.isSelected }

                if (selectedProviders.isEmpty()) {
                    _errorMessage.value = "Please select at least one provider"
                    _isLoading.value = false
                    return@launch
                }

                // Add each selected provider as a sender
                selectedProviders.forEach { provider ->
                    val addSenderResult = addSenderUseCase(
                        senderId = provider.senderId,
                        displayName = provider.displayName
                    )

                    if (addSenderResult.isError) {
                        throw addSenderResult.exceptionOrNull() ?: IllegalStateException("Failed to add sender")
                    }

                    ProviderTemplateDefaults.forSender(provider.senderId).forEach { preset ->
                        val templateResult = senderRepository.addTemplate(
                            SenderTemplate(
                                senderId = provider.senderId,
                                label = preset.label,
                                pattern = preset.pattern,
                                createdAt = DateTimeUtils.getCurrentTimestamp()
                            )
                        )

                        if (templateResult.isError) {
                            throw templateResult.exceptionOrNull()
                                ?: IllegalStateException("Failed to add template")
                        }
                    }
                }

                // Mark onboarding as complete
                preferencesManager.setOnboardingComplete()

                Timber.d("Provider selection saved: ${selectedProviders.size} providers")
                _saveComplete.value = true
            } catch (e: Exception) {
                Timber.e(e, "Error saving provider selections")
                _errorMessage.value = "Failed to save selections. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Skip onboarding without selecting providers
     */
    fun skip() {
        preferencesManager.setOnboardingComplete()
        _saveComplete.value = true
    }
}
