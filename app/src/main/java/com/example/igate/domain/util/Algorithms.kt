package com.example.igate.domain.util

object Algorithms {

    /**
     * Calculates the Levenshtein distance between two strings.
     * This is an advanced string metric for measuring the difference between two sequences.
     * It represents the minimum number of single-character edits (insertions, deletions or substitutions)
     * required to change one word into the other.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val a = s1.lowercase()
        val b = s2.lowercase()
        
        val costs = IntArray(b.length + 1)
        for (j in 0..b.length) {
            costs[j] = j
        }
        
        for (i in 1..a.length) {
            costs[0] = i
            var nw = i - 1
            for (j in 1..b.length) {
                val cj = Math.min(
                    1 + Math.min(costs[j], costs[j - 1]),
                    if (a[i - 1] == b[j - 1]) nw else nw + 1
                )
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[b.length]
    }

    /**
     * A basic fuzzy search function that uses Levenshtein distance.
     * Returns true if the query is a close match (e.g., small edit distance) to the target string.
     */
    fun fuzzyMatch(query: String, target: String, maxDistance: Int = 2): Boolean {
        if (query.isBlank()) return true
        
        // Direct substring match is always an instant hit
        if (target.lowercase().contains(query.lowercase())) return true

        // Otherwise, split target into words and see if any word is close to the query
        val targetWords = target.split(" ", "-", "_")
        for (word in targetWords) {
            if (word.length >= 3 && levenshteinDistance(query, word) <= maxDistance) {
                return true
            }
        }
        return false
    }
}
