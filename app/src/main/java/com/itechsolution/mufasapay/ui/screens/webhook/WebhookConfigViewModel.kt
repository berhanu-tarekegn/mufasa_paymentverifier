package com.itechsolution.mufasapay.ui.screens.webhook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itechsolution.mufasapay.domain.model.WebhookConfig
import com.itechsolution.mufasapay.domain.usecase.webhook.GetWebhookConfigUseCase
import com.itechsolution.mufasapay.domain.usecase.webhook.SaveWebhookConfigUseCase
import com.itechsolution.mufasapay.domain.usecase.webhook.TestWebhookConnectionUseCase
import com.itechsolution.mufasapay.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for webhook configuration screen
 */
class WebhookConfigViewModel(
    private val getWebhookConfigUseCase: GetWebhookConfigUseCase,
    private val saveWebhookConfigUseCase: SaveWebhookConfigUseCase,
    private val testWebhookConnectionUseCase: TestWebhookConnectionUseCase
) : ViewModel() {

    // Form fields
    private val _uploadUrl = MutableStateFlow("")
    val uploadUrl: StateFlow<String> = _uploadUrl.asStateFlow()

    private val _deleteUrlTemplate = MutableStateFlow("")
    val deleteUrlTemplate: StateFlow<String> = _deleteUrlTemplate.asStateFlow()

    private val _headers = MutableStateFlow<Map<String, String>>(emptyMap())
    val headers: StateFlow<Map<String, String>> = _headers.asStateFlow()

    private val _authType = MutableStateFlow("NONE")
    val authType: StateFlow<String> = _authType.asStateFlow()

    private val _authValue = MutableStateFlow("")
    val authValue: StateFlow<String> = _authValue.asStateFlow()

    private val _timeout = MutableStateFlow(30000)
    val timeout: StateFlow<Int> = _timeout.asStateFlow()

    private val _retryEnabled = MutableStateFlow(true)
    val retryEnabled: StateFlow<Boolean> = _retryEnabled.asStateFlow()

    private val _maxRetries = MutableStateFlow(3)
    val maxRetries: StateFlow<Int> = _maxRetries.asStateFlow()

    private val _retryDelayMs = MutableStateFlow(5000L)
    val retryDelayMs: StateFlow<Long> = _retryDelayMs.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    // UI states
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadConfig()
    }

    /**
     * Load existing webhook configuration
     */
    private fun loadConfig() {
        viewModelScope.launch {
            getWebhookConfigUseCase().collect { config ->
                config?.let {
                    _uploadUrl.value = it.uploadUrl
                    _deleteUrlTemplate.value = it.deleteUrlTemplate
                    _headers.value = it.headers
                    _authType.value = it.authType
                    _authValue.value = it.authValue ?: ""
                    _timeout.value = it.timeout
                    _retryEnabled.value = it.retryEnabled
                    _maxRetries.value = it.maxRetries
                    _retryDelayMs.value = it.retryDelayMs
                    _isEnabled.value = it.isEnabled
                }
            }
        }
    }

    // Update methods
    fun updateUploadUrl(value: String) { _uploadUrl.value = value }
    fun updateDeleteUrlTemplate(value: String) { _deleteUrlTemplate.value = value }
    fun updateAuthType(value: String) { _authType.value = value }
    fun updateAuthValue(value: String) { _authValue.value = value }
    fun updateTimeout(value: Int) { _timeout.value = value }
    fun updateRetryEnabled(value: Boolean) { _retryEnabled.value = value }
    fun updateMaxRetries(value: Int) { _maxRetries.value = value }
    fun updateRetryDelayMs(value: Long) { _retryDelayMs.value = value }
    fun updateIsEnabled(value: Boolean) { _isEnabled.value = value }

    fun addHeader(key: String, value: String) {
        if (key.isNotBlank() && value.isNotBlank()) {
            _headers.value = _headers.value + (key to value)
        }
    }

    fun removeHeader(key: String) {
        _headers.value = _headers.value - key
    }

    /**
     * Validate and save webhook configuration
     */
    fun saveConfig() {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            if (!validateUrl(_uploadUrl.value)) {
                _errorMessage.value = "Invalid upload URL. Must start with http:// or https://"
                _isSaving.value = false
                return@launch
            }

            if (!validateDeleteUrlTemplate(_deleteUrlTemplate.value)) {
                _errorMessage.value = "Invalid delete URL. Must start with http:// or https:// and include {transaction_id}"
                _isSaving.value = false
                return@launch
            }

            if (_timeout.value <= 0) {
                _errorMessage.value = "Timeout must be greater than 0"
                _isSaving.value = false
                return@launch
            }

            if (_maxRetries.value < 0 || _maxRetries.value > 10) {
                _errorMessage.value = "Max retries must be between 0 and 10"
                _isSaving.value = false
                return@launch
            }

            when (val result = saveWebhookConfigUseCase(
                uploadUrl = _uploadUrl.value,
                deleteUrlTemplate = _deleteUrlTemplate.value,
                headers = _headers.value,
                authType = _authType.value,
                authValue = _authValue.value.ifBlank { null },
                timeout = _timeout.value,
                retryEnabled = _retryEnabled.value,
                maxRetries = _maxRetries.value,
                retryDelayMs = _retryDelayMs.value,
                isEnabled = _isEnabled.value
            )) {
                is Result.Success -> {
                    _successMessage.value = "Webhook configuration saved successfully"
                    Timber.d("Webhook config saved")
                }
                is Result.Error -> {
                    val message = result.message ?: "Failed to save configuration"
                    _errorMessage.value = message
                    Timber.e(result.exception, message)
                }

                else -> {}
            }

            _isSaving.value = false
        }
    }

    /**
     * Test webhook connection
     */
    fun testConnection() {
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = null

            when (val result = testWebhookConnectionUseCase()) {
                is Result.Success -> {
                    _testResult.value = "✓ Connection successful! Response: ${result.data}"
                    _successMessage.value = "Webhook test successful"
                }
                is Result.Error -> {
                    _testResult.value = "✗ Connection failed: ${result.message}"
                    _errorMessage.value = result.message ?: "Connection test failed"
                }

                else -> {}
            }

            _isTesting.value = false
        }
    }

    /**
     * Validate URL format
     */
    private fun validateUrl(url: String): Boolean {
        return url.matches(Regex("^https?://.*"))
    }

    private fun validateDeleteUrlTemplate(url: String): Boolean {
        return validateUrl(url) && url.contains("{transaction_id}")
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearTestResult() {
        _testResult.value = null
    }
}
