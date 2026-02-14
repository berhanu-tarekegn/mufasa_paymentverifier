package com.itechsolution.mufasapay.ui.screens.permissions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for permission management screen
 */
class PermissionViewModel : ViewModel() {

    private val _smsPermissionGranted = MutableStateFlow(false)
    val smsPermissionGranted: StateFlow<Boolean> = _smsPermissionGranted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted.asStateFlow()

    /**
     * Update SMS permission status
     */
    fun updateSmsPermissionStatus(granted: Boolean) {
        _smsPermissionGranted.value = granted
    }

    /**
     * Update notification permission status
     */
    fun updateNotificationPermissionStatus(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    /**
     * Check if all required permissions are granted
     */
    fun areAllPermissionsGranted(): Boolean {
        return _smsPermissionGranted.value && _notificationPermissionGranted.value
    }
}
