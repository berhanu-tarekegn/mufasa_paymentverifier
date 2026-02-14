package com.itechsolution.mufasapay.ui.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app preferences, including first-run onboarding status
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Check if this is the first run (onboarding not completed)
     */
    fun isFirstRun(): Boolean {
        return !prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    /**
     * Mark onboarding as complete
     */
    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    /**
     * Reset onboarding status (for testing)
     */
    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, false).apply()
    }

    companion object {
        private const val PREFS_NAME = "mufasapay_prefs"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
