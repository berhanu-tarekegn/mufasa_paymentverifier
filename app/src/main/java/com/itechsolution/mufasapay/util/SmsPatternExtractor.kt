package com.itechsolution.mufasapay.util

import timber.log.Timber
import java.util.regex.PatternSyntaxException

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

    // Regex to find the first numeric amount in a message.
    private val AMOUNT_REGEX = Regex("""\b\d[\d,.]*\b""")

    private val MONEY_RECEIVED_REGEX = Regex(
        """(?i)${receiveKeywords.joinToString("|")}""",
        RegexOption.IGNORE_CASE
    )


    /**
     * Extracts the full phrase from a sample message before the first amount.
     * For example, "Dear John, you have received ETB 100.00..."
     * becomes "Dear John, you have received ETB".
     * You can then manually edit this to "Dear {name}, you have received ETB".
     */
    fun extractPattern(sampleMessage: String): String? {
        val message = sampleMessage.trim()
        if (message.isBlank()) return null

        // 1. Ensure this is a "money received" message.
        if (!MONEY_RECEIVED_REGEX.containsMatchIn(message)) {
            Timber.d("Message does not contain any receive keywords. Cannot extract pattern.")
            return null
        }

        // 2. Find the first numeric amount.
        val amountMatch = AMOUNT_REGEX.find(message)
        if (amountMatch == null) {
            Timber.d("Message does not contain a numeric amount. Cannot extract pattern.")
            // As a fallback, we could return the first line, but it might not be a good pattern.
            // Returning null is safer.
            return null
        }

        // 3. Extract the text before the amount.
        val pattern = message.substring(0, amountMatch.range.first).trim()

        // 4. Basic validation to ensure the pattern is useful.
        if (pattern.length < 5 || pattern.length > 100) {
            Timber.d("Extracted pattern is too short or too long: '$pattern'")
            return null
        }

        return pattern
    }

    /** General fallback regex for detecting money-received messages */
    val MONEY_RECEIVED_FALLBACK_REGEX = Regex(
        """(?i)(received|credited|deposited|transferred to you|sent to you|incoming transfer)""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Converts a user-defined pattern with placeholders into a valid Regex.
     * For example, "Dear {name}, you received {amount}" becomes a functional regex.
     */
    private fun convertToRegex(pattern: String): Regex {
        val placeholders = mapOf(
            "{name}" to "(.+?)",
            "{amount}" to """([\d,]+\.?\d*)""",
            "{account}" to """([\d\*]+)""",
            "{phone}" to """(\+?[\d\*]+)""",
            "{datetime}" to "(.+?)",
            "{transaction}" to """([\w\*]+)""",
            "{balance}" to """([\d,]+\.?\d*)""",
            "{ignore}" to "(.*?)"
        )

        val resultRegex = StringBuilder()
        var currentIdx = 0

        // Find all placeholder occurrences
        val placeholderMatches = mutableListOf<Pair<Int, String>>()
        placeholders.keys.forEach { placeholder ->
            var idx = pattern.indexOf(placeholder)
            while (idx != -1) {
                placeholderMatches.add(idx to placeholder)
                idx = pattern.indexOf(placeholder, idx + 1)
            }
        }

        // Sort by position to process sequentially
        placeholderMatches.sortBy { it.first }

        placeholderMatches.forEach { (idx, placeholder) ->
            if (idx > currentIdx) {
                // Escape the literal text before the placeholder
                resultRegex.append(Regex.escape(pattern.substring(currentIdx, idx)))
            }
            // Append the regex for the placeholder
            resultRegex.append(placeholders[placeholder])
            currentIdx = idx + placeholder.length
        }

        if (currentIdx < pattern.length) {
            // Escape any remaining literal text
            resultRegex.append(Regex.escape(pattern.substring(currentIdx)))
        }

        // Using DOT_MATCHES_ALL to allow '.' to match newline characters like '\n'
        return Regex(resultRegex.toString(), setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    }

    /**
     * Checks if a message matches a sender's pattern.
     * If pattern is null/empty, falls back to general money-received detection.
     * It now supports regex with placeholders like {name}, {amount}.
     */
    fun matchesPattern(message: String, pattern: String?): Boolean {
        return if (!pattern.isNullOrBlank()) {
            try {
                // Convert the user-friendly pattern to a real regex and check for a match.
                val regex = convertToRegex(pattern)
                regex.containsMatchIn(message)
            } catch (e: PatternSyntaxException) {
                Timber.e(e, "Invalid pattern syntax: $pattern. Falling back to simple 'contains' check.")
                // If the pattern is not a valid regex, fall back to a simple string contains check.
                message.contains(pattern, ignoreCase = true)
            }
        } else {
            MONEY_RECEIVED_REGEX.containsMatchIn(message)
        }
    }
}
