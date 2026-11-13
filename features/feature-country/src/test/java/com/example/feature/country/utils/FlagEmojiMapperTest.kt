package com.example.feature.country.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FlagEmojiMapperTest {

    @Test
    fun `converts two letter codes to regional indicator flags`() {
        assertEquals("🇺🇸", countryCodeToFlagEmoji("US"))
        assertEquals("🇯🇵", countryCodeToFlagEmoji("jp"))
    }

    @Test
    fun `invalid codes return the placeholder flag`() {
        listOf(null, "", "U", "USA", "U1", "é!").forEach { code ->
            assertEquals("for $code", "🏳", countryCodeToFlagEmoji(code))
        }
    }
}
