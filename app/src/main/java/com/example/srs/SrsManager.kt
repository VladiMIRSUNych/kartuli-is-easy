package com.example.srs

import com.example.data.WordEntity

enum class SrsRating(val score: Int, val label: String) {
    AGAIN(1, "Снова"),
    HARD(3, "Трудно"),
    GOOD(4, "Хорошо"),
    EASY(5, "Легко")
}

object SrsManager {

    private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    /**
     * Calculates the new SRS status according to the SuperMemo SM-2 spaced repetition algorithm.
     */
    fun calculateNextReview(word: WordEntity, rating: SrsRating): WordEntity {
        val q = rating.score
        var repetitions = word.repetitions
        var intervalDays = word.intervalDays
        var easeFactor = word.easeFactor

        if (q < 3) {
            // Failed recall: reset repetitions and restart with 1 day
            repetitions = 0
            intervalDays = 1
        } else {
            // Successful recall
            when (repetitions) {
                0 -> intervalDays = 1
                1 -> intervalDays = 6
                else -> intervalDays = (intervalDays * easeFactor).toInt().coerceAtLeast(1)
            }
            repetitions += 1
            if (rating == SrsRating.EASY) {
                intervalDays = (intervalDays * 1.3f).toInt().coerceAtLeast(intervalDays + 1)
            }
        }

        // Update Ease Factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        easeFactor = (easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))).coerceAtLeast(1.3f)

        val now = System.currentTimeMillis()
        val nextReviewTimestamp = now + (intervalDays * ONE_DAY_MS)

        return word.copy(
            repetitions = repetitions,
            intervalDays = intervalDays,
            easeFactor = easeFactor,
            lastReviewedTimestamp = now,
            nextReviewTimestamp = nextReviewTimestamp,
            learned = repetitions >= 2
        )
    }
}
