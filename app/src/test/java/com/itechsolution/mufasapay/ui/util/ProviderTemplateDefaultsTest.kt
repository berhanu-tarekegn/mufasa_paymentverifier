package com.itechsolution.mufasapay.ui.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderTemplateDefaultsTest {

    @Test
    fun `known provider exposes default templates`() {
        val templates = ProviderTemplateDefaults.forSender("CBE BIRR")

        assertFalse("CBE BIRR should ship with at least one default template", templates.isEmpty())
        assertTrue(
            "Default template should include placeholder-based pattern",
            templates.any { it.pattern.contains("{amount}") }
        )
    }

    @Test
    fun `unknown provider returns empty defaults`() {
        val templates = ProviderTemplateDefaults.forSender("UNKNOWN")

        assertTrue("Unknown providers should not get fabricated defaults", templates.isEmpty())
    }
}
