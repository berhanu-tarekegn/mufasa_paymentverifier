package com.itechsolution.mufasapay.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private const val PATTERN_FULL = "yyyy-MM-dd HH:mm:ss"
    private const val PATTERN_DATE_ONLY = "yyyy-MM-dd"
    private const val PATTERN_TIME_ONLY = "HH:mm:ss"
    private const val PATTERN_DISPLAY = "MMM dd, yyyy HH:mm"

    private val fullFormatter = SimpleDateFormat(PATTERN_FULL, Locale.getDefault())
    private val dateFormatter = SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault())
    private val timeFormatter = SimpleDateFormat(PATTERN_TIME_ONLY, Locale.getDefault())
    private val displayFormatter = SimpleDateFormat(PATTERN_DISPLAY, Locale.getDefault())

    fun formatFull(timestamp: Long): String {
        return fullFormatter.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }

    fun formatDisplay(timestamp: Long): String {
        return displayFormatter.format(Date(timestamp))
    }

    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> formatDisplay(timestamp)
        }
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun isOlderThan(timestamp: Long, days: Int): Boolean {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return timestamp < cutoff
    }

    /**
     * Format timestamp as relative time (e.g., "5 minutes ago")
     * Alias for getTimeAgo for consistency with UI code
     */
    fun formatRelativeTime(timestamp: Long): String {
        return getTimeAgo(timestamp)
    }
}
