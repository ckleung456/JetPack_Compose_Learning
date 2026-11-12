package com.example.feature.country.utils

private const val REGIONAL_INDICATOR_BASE = 0x1F1E6
private const val PLACEHOLDER_FLAG = "🏳"

fun countryCodeToFlagEmoji(code: String?): String {
    if (code == null || code.length != 2) return PLACEHOLDER_FLAG
    val first = code[0].uppercaseChar()
    val second = code[1].uppercaseChar()
    if (first !in 'A'..'Z' || second !in 'A'..'Z') return PLACEHOLDER_FLAG

    return codePointToString(REGIONAL_INDICATOR_BASE + (first - 'A')) +
            codePointToString(REGIONAL_INDICATOR_BASE + (second - 'A'))
}

private fun codePointToString(codePoint: Int): String {
    if (codePoint <= 0xFFFF) return codePoint.toChar().toString()
    val offset = codePoint - 0x10000
    val high = (offset shr 10) + 0xD800
    val low = (offset and 0x3FF) + 0xDC00
    return charArrayOf(high.toChar(), low.toChar()).concatToString()
}