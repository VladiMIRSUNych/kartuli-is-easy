package com.example

import com.example.data.WordEntity
import com.example.srs.SrsManager
import com.example.srs.SrsRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsManagerTest {

    @Test
    fun testFirstGoodRecallSetsIntervalToOneDay() {
        val word = WordEntity(
            georgian = "გამარჯობა",
            transcription = "gamarjoba",
            russian = "здравствуйте",
            category = "greetings"
        )

        val updated = SrsManager.calculateNextReview(word, SrsRating.GOOD)
        assertEquals(1, updated.repetitions)
        assertEquals(1, updated.intervalDays)
        assertTrue(updated.nextReviewTimestamp > System.currentTimeMillis())
    }

    @Test
    fun testSecondGoodRecallSetsIntervalToSixDays() {
        val word = WordEntity(
            georgian = "მადლობა",
            transcription = "madloba",
            russian = "спасибо",
            category = "greetings",
            repetitions = 1,
            intervalDays = 1,
            easeFactor = 2.5f
        )

        val updated = SrsManager.calculateNextReview(word, SrsRating.GOOD)
        assertEquals(2, updated.repetitions)
        assertEquals(6, updated.intervalDays)
        assertTrue(updated.learned)
    }

    @Test
    fun testFailedRecallResetsRepetitions() {
        val word = WordEntity(
            georgian = "ღვინო",
            transcription = "ghvino",
            russian = "вино",
            category = "food",
            repetitions = 4,
            intervalDays = 15,
            easeFactor = 2.5f
        )

        val updated = SrsManager.calculateNextReview(word, SrsRating.AGAIN)
        assertEquals(0, updated.repetitions)
        assertEquals(1, updated.intervalDays)
    }
}
