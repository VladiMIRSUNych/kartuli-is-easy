package com.example

import com.example.data.WordEntity
import com.example.util.TranslationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DictionaryAndTranslationTest {

    @Test
    fun testTranslationValidationSynonymsAndCase() {
        val word = WordEntity(
            georgian = "მადლობა",
            transcription = "madloba / мадлоба",
            russian = "спасибо",
            synonyms = "спасибо, благодарю, большое спасибо",
            partOfSpeech = "PHRASE"
        )

        // Lowercase, uppercase, titlecase
        assertTrue(TranslationValidator.isRussianTranslationCorrect("спасибо", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("Спасибо", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("СПАСИБО", word.russian, word.getSynonymList()))

        // Synonyms
        assertTrue(TranslationValidator.isRussianTranslationCorrect("благодарю", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("Благодарю", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("Большое спасибо", word.russian, word.getSynonymList()))

        // Missing or extra punctuation
        assertTrue(TranslationValidator.isRussianTranslationCorrect("Спасибо!", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("благодарю...", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect(" благодарю ", word.russian, word.getSynonymList()))

        // Incorrect answers
        assertFalse(TranslationValidator.isRussianTranslationCorrect("привет", word.russian, word.getSynonymList()))
        assertFalse(TranslationValidator.isRussianTranslationCorrect("до свидания", word.russian, word.getSynonymList()))
    }

    @Test
    fun testCaseAndPunctuationInsensitiveAdjective() {
        val word = WordEntity(
            georgian = "ლამაზი",
            transcription = "lamazi / ламази",
            russian = "красивый",
            synonyms = "красивый, прекрасный",
            partOfSpeech = "ADJECTIVE"
        )

        assertTrue(TranslationValidator.isRussianTranslationCorrect("Красивый", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("красивый", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("красивый!", word.russian, word.getSynonymList()))
        assertTrue(TranslationValidator.isRussianTranslationCorrect("Прекрасный", word.russian, word.getSynonymList()))
    }

    @Test
    fun testGeorgianToCyrillicPhonetics() {
        val map = mapOf(
            'ა' to "а", 'ბ' to "б", 'გ' to "г", 'დ' to "д", 'ე' to "э",
            'ვ' to "в", 'ზ' to "з", 'თ' to "т", 'ი' to "и", 'კ' to "к",
            'ლ' to "л", 'მ' to "м", 'ნ' to "н", 'ო' to "о", 'პ' to "п",
            'ჟ' to "ж", 'რ' to "р", 'ს' to "с", 'ტ' to "т", 'უ' to "у",
            'ფ' to "п", 'ქ' to "к", 'ღ' to "г", 'ყ' to "к", 'შ' to "ш",
            'ჩ' to "ч", 'ც' to "ц", 'ძ' to "дз", 'წ' to "ц", 'ჭ' to "ч",
            'ხ' to "х", 'ჯ' to "дж", 'ჰ' to "х"
        )
        fun toPhonetics(text: String): String {
            val sb = StringBuilder()
            for (ch in text) sb.append(map[ch] ?: ch)
            return sb.toString()
        }

        assertEquals("гтховт", toPhonetics("გთხოვთ"))
        assertEquals("ара", toPhonetics("არა"))
        assertEquals("гамарджоба", toPhonetics("გამარჯობა"))
        assertEquals("мадлоба", toPhonetics("მადლობა"))
        assertEquals("рдзэ", toPhonetics("რძე"))
    }

    @Test
    fun testWordSubFormsAccountingAndEncounter() {
        val word = WordEntity(
            georgian = "რძე",
            transcription = "rdze / рдзе",
            russian = "молоко",
            partOfSpeech = "NOUN",
            subFormsJson = """[
                {"georgian":"რძე","transcription":"rdze","russian":"молоко","grammaticalNote":"им. пад.","isEncountered":true},
                {"georgian":"რძით","transcription":"rdzit","russian":"с молоком","grammaticalNote":"творит. пад.","isEncountered":false}
            ]"""
        )

        assertEquals(2, word.getSubForms().size)
        assertEquals(1, word.getEncounteredSubForms().size)
        assertEquals("რძე", word.getEncounteredSubForms().first().georgian)

        val updated = word.withFormEncountered("რძით")
        assertEquals(2, updated.getEncounteredSubForms().size)
        assertTrue(updated.getEncounteredSubForms().any { it.georgian == "რძით" })
    }
}
