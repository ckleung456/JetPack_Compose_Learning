package com.example.shared.lib.utils

import com.example.shared.lib.utils.Algorithm2.Something
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Algorithm2Test {

    private val input: Array<Any> = arrayOf(
        "Apple", 42, Something("1", "Alice"), 42, Something("1", "Alice"), Something("2", "Bob")
    )
    private val expected: Array<Any> = arrayOf("Apple", 42, Something("1", "Alice"), Something("2", "Bob"))

    @Test
    fun `dedupe solutions keep first occurrence order`() {
        assertArrayEquals(expected, Algorithm2.solutionTwo(input))
        assertArrayEquals(expected, Algorithm2.solutionThree(input))
        assertArrayEquals(expected, Algorithm2.solutionFour(input))
    }

    @Test
    fun `dedupe solutions handle empty input`() {
        assertEquals(0, Algorithm2.solutionTwo(emptyArray()).size)
        assertEquals(0, Algorithm2.solutionThree(emptyArray()).size)
        assertEquals(0, Algorithm2.solutionFour(emptyArray()).size)
    }

    @Test
    fun `maxWater finds the largest container`() {
        assertEquals(49, Algorithm2.maxWater(intArrayOf(1, 8, 6, 2, 5, 4, 8, 3, 7)))
        assertEquals(1, Algorithm2.maxWater(intArrayOf(1, 1)))
        assertEquals(0, Algorithm2.maxWater(intArrayOf(5)))
    }
}
