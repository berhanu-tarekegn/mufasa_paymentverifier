package com.itechsolution.mufasapay.ui.state

/**
 * Generic UI state wrapper for handling loading, success, error states
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Exception? = null) : UiState<Nothing>()
}
