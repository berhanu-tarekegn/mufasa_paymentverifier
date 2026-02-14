package com.itechsolution.mufasapay.util

/**
 * Extracts a matching pattern from a sample SMS message.
 * Takes the opening phrase (everything before the first amount/number)
 * and uses it as the pattern to identify similar messages.
 *
 * Example:
 *   "You have received ETB 1,500.00 from ABEBE KEBEDE"
 *   → "You have received ETB"
 */
object SmsPatternExtractor {

    // Matches amounts like: 1500, 1,500, 1500.00, 1,500.00
    val receiveKeywords = listOf(
        "received", "credited", "deposit", "added",
        "funds received", "payment received",
        "transferred to you", "incoming funds",
        "amount credited", "money received",
        "top-up", "cash-in", "wallet funded", "deposit confirmed"
    )


    private val AMOUNT_REGEX = Regex("""[\d,]+\.?\d*""")
    private val MONEY_RECEIVED_REGEX = Regex(
        """(?i)${receiveKeywords.joinToString("|")}""",
        RegexOption.IGNORE_CASE
    )


    /**
     * Extracts the opening phrase from a sample message.
     * Returns the text before the first numeric amount, trimmed.
     * Returns null if extraction fails or result is too short.
     */
    fun extractPattern(sampleMessage: String): String? {
        var message = sampleMessage.trim()
        if (message.isBlank()) return null

        // 1️⃣ Find the first receive keyword
        val keywordMatch = receiveKeywords
            .mapNotNull { kw -> Regex("""(?i)\b$kw\b""").find(message)?.range?.first?.let { it to kw } }
            .minByOrNull { it.first }

        if (keywordMatch == null) return null

        val startIndex = keywordMatch.first
        val keyword = keywordMatch.second

        // 5️⃣ Trim extra spaces
        return keyword
    }

    /** General fallback regex for detecting money-received messages */
    val MONEY_RECEIVED_FALLBACK_REGEX = Regex(
        """(?i)(received|credited|deposited|transferred to you|sent to you|incoming transfer)""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Checks if a message matches a sender's pattern.
     * If pattern is null/empty, falls back to general money-received detection.
     */
    fun matchesPattern(message: String, pattern: String?): Boolean {
        return if (!pattern.isNullOrBlank()) {
            message.contains(pattern, ignoreCase = true)
        } else {
            MONEY_RECEIVED_REGEX.containsMatchIn(message)
        }
    }
}
