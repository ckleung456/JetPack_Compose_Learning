package com.example.core.utils

object Algorithm2 {
    private val set = mutableSetOf<Any>()

    data class Something(val id: String, val name: String)

    fun solutionOne(
        input: Array<Any> = arrayOf(
            "Apple",
            42,
            Something("1", "Alice"),
            42,
            Something("1", "Alice"),
            Something("2", "Bob"),
        )
    ): Array<Any> {
        if (input.isEmpty()) return emptyArray()

        input.forEach {
            set.add(it)
        }

        return set.toTypedArray()
    }

    fun solutionTwo(
        input: Array<Any> = arrayOf(
            "Apple",
            42,
            Something("1", "Alice"),
            42,
            Something("1", "Alice"),
            Something("2", "Bob"),
        )
    ): Array<Any> {
        return input.distinct().toTypedArray()
    }

    fun solutionThree(
        input: Array<Any> = arrayOf(
            "Apple",
            42,
            Something("1", "Alice"),
            42,
            Something("1", "Alice"),
            Something("2", "Bob"),
        )
    ): Array<Any> {
        val result = mutableListOf<Any>()
        input.forEach { item ->
            if (item !in result) {
                result.add(item)
            }
        }

        return result.toTypedArray()
    }

    fun solutionFour(
        input: Array<Any> = arrayOf(
            "Apple",
            42,
            Something("1", "Alice"),
            42,
            Something("1", "Alice"),
            Something("2", "Bob"),
        )
    ): Array<Any> {
        val map = mutableMapOf<Any, Int>()

        input.forEach { item ->
            map[item]?.let { count ->
                map[item] = count + 1
            } ?: run {
                map[item] = 1
            }
        }

        return map.keys.toTypedArray()
    }

    fun maxWater(
        height: IntArray = intArrayOf(
            1, 8, 6, 2, 5, 4, 8, 3, 7
        )
    ): Int {
        var left = 0
        var right = height.size - 1
        var max = 0

        while(left < right) {
            val h = minOf(height[left], height[right])
            val width = right - left
            max = maxOf(max, h * width)

            if (height[left] < height[right]) left++ else right--
        }

        return max
    }
}