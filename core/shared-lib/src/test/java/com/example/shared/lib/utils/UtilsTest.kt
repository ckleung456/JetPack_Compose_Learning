package com.example.shared.lib.utils

import com.example.shared.lib.utils.Utils.asSafeList
import com.example.shared.lib.utils.Utils.isListOfType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun `isListOfType is true only when every element matches`() {
        assertTrue(listOf("a", "b").isListOfType<String>())
        assertTrue(emptyList<Int>().isListOfType<String>())
        assertFalse(listOf("a", 1).isListOfType<String>())
    }

    @Test
    fun `isListOfType is false for non lists and null`() {
        assertFalse(setOf("a").isListOfType<String>())
        assertFalse("a".isListOfType<String>())
        assertFalse(null.isListOfType<String>())
    }

    @Test
    fun `asSafeList casts matching lists and rejects others`() {
        assertEquals(listOf(1, 2), listOf(1, 2).asSafeList<Int>())
        assertNull(listOf(1, "2").asSafeList<Int>())
        assertNull("not a list".asSafeList<Int>())
    }
}
