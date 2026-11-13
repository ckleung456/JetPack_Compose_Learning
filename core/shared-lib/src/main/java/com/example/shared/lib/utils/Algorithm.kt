package com.example.shared.lib.utils

object Algorithm {
    fun testPath() {
        val (path, isPathExist) = getPath(
            words = listOf("dog", "grin", "rad", "gum", "nag"),
            start = "rad",
            end = "gum"
        )
        if (isPathExist) {
            val sb = StringBuilder()
            sb.append("path exists ")
            sb.append(path)
            println(sb.toString())
        } else {
            println("path does not exist")
        }
    }

    private fun getPath(words: List<String>, start: String, end: String): Pair<List<String>, Boolean> {
        if (words.isEmpty() || start.isEmpty() || end.isEmpty() ||
            start !in words || end !in words) {
            return emptyList<String>() to false
        }
        if (start == end) return listOf(start) to true

        // Create a map to group words by their first character Map<Char, MutableSet<String>>
        val wordsByFirstChar = words.groupBy { it.first() }
            .mapValues { (_, wordList) -> wordList.toMutableSet() }

        // BFS to find the shortest path
        val queue = ArrayDeque<List<String>>()
        val visited = mutableSetOf<String>()

        queue.add(listOf(start))
        visited.add(start)

        while (queue.isNotEmpty()) {
            val currentPath = queue.removeFirst()
            val currentWord = currentPath.last()
            val lastChar = currentWord.last()

            val nextWords = wordsByFirstChar[lastChar] ?: continue

            if (end in nextWords) return currentPath + end to true

            for (nextWord in nextWords) {
                if (nextWord !in visited) {
                    queue.add(currentPath + nextWord)
                    visited.add(nextWord)
                }
            }
        }

        return emptyList<String>() to false
    }


}