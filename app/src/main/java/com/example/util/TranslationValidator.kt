package com.example.util

object TranslationValidator {

    /**
     * Normalizes text for comparison:
     * - Lowercase
     * - Strips all punctuation marks (.,!?-:;"'«»— etc.)
     * - Collapses whitespace
     */
    fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[\\p{Punct}«»—–…]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Checks if the user's translation matches the Russian target or any of its synonyms.
     * Supports:
     * - Case insensitivity ("Красивый" matches "красивый")
     * - Punctuation insensitivity (omitted or extra punctuation doesn't fail the check)
     * - Synonyms and multiple translations (e.g. "madloba" matches "спасибо", "благодарю", "большое спасибо")
     * - Comma / slash separated alternatives in primary target
     */
    fun isRussianTranslationCorrect(
        userInput: String,
        primaryRussian: String,
        synonyms: List<String> = emptyList()
    ): Boolean {
        val userNorm = normalize(userInput)
        if (userNorm.isBlank()) return false

        val candidates = mutableSetOf<String>()

        // 1. Primary Russian splits
        for (part in primaryRussian.split('/', ',', ';')) {
            val n = normalize(part)
            if (n.isNotBlank()) candidates.add(n)
        }
        val fullPrimary = normalize(primaryRussian)
        if (fullPrimary.isNotBlank()) candidates.add(fullPrimary)

        // 2. Synonyms
        for (syn in synonyms) {
            for (part in syn.split('/', ',', ';')) {
                val n = normalize(part)
                if (n.isNotBlank()) candidates.add(n)
            }
        }

        // Direct normalized match
        if (candidates.contains(userNorm)) return true

        val userTokens = userNorm.split(' ').filter { it.isNotBlank() }.sorted()

        // Check if any candidate equals userNorm or matches word-for-word regardless of order
        for (candidate in candidates) {
            if (candidate == userNorm) return true
            val candTokens = candidate.split(' ').filter { it.isNotBlank() }.sorted()
            if (candTokens.isNotEmpty() && candTokens == userTokens) return true
        }

        return false
    }

    /**
     * Checks if user entered valid Georgian (either Mkhedruli script or natural transliteration).
     */
    fun isGeorgianTranslationCorrect(
        userInput: String,
        targetGeorgian: String,
        transcription: String = "",
        phoneticCyrillic: String = ""
    ): Boolean {
        val userNorm = normalize(userInput)
        val targetNorm = normalize(targetGeorgian)
        if (userNorm == targetNorm) return true

        // Check Latin transcription parts
        if (transcription.isNotBlank()) {
            val parts = transcription.split('/', ',').map { normalize(it) }
            if (parts.any { it == userNorm || it.replace(" ", "") == userNorm.replace(" ", "") }) {
                return true
            }
        }

        // Check Cyrillic transcription parts
        if (phoneticCyrillic.isNotBlank()) {
            val parts = phoneticCyrillic.split('/', ',').map { normalize(it) }
            if (parts.any { it == userNorm || it.replace(" ", "") == userNorm.replace(" ", "") }) {
                return true
            }
        }

        return false
    }
}
