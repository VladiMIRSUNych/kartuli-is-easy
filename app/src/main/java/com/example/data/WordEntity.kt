package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class PartOfSpeech(val code: String, val labelRu: String) {
    NOUN("NOUN", "Существительное"),
    VERB("VERB", "Глагол"),
    ADJECTIVE("ADJECTIVE", "Прилагательное"),
    PRONOUN("PRONOUN", "Местоимение"),
    NUMERAL("NUMERAL", "Числительное"),
    CONJUNCTION("CONJUNCTION", "Союз"),
    PARTICLE("PARTICLE", "Частица"),
    ADVERB("ADVERB", "Наречие"),
    PHRASE("PHRASE", "Этикет и фразы");

    companion object {
        fun fromCode(code: String): PartOfSpeech {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: NOUN
        }
    }
}

data class WordSubForm(
    val georgian: String,
    val transcription: String,
    val russian: String,
    val grammaticalNote: String,
    val isEncountered: Boolean = false
)

@Entity(
    tableName = "words",
    indices = [androidx.room.Index(value = ["georgian"], unique = true)]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val georgian: String,                // Base word e.g. "რძე" or "გამარჯობა"
    val transcription: String,           // e.g. "rdze / рдзе"
    val russian: String,                 // e.g. "молоко"
    val synonyms: String = "",           // comma-separated synonyms e.g. "спасибо, благодарю"
    val partOfSpeech: String = "NOUN",   // PartOfSpeech code
    val category: String = "general",    // e.g. "food", "greetings", "city", etc.
    val emoji: String = "🇬🇪",
    val exampleGeorgian: String = "",
    val exampleRussian: String = "",
    val subFormsJson: String = "",       // JSON representation of List<WordSubForm>
    val isCustom: Boolean = false,

    // Spaced Repetition (SM-2) Fields
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewTimestamp: Long = 0L,
    val lastReviewedTimestamp: Long = 0L,
    val learned: Boolean = false
) {
    fun getPartOfSpeechEnum(): PartOfSpeech = PartOfSpeech.fromCode(partOfSpeech)

    fun getSubForms(): List<WordSubForm> {
        if (subFormsJson.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(subFormsJson)
            val list = mutableListOf<WordSubForm>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WordSubForm(
                        georgian = obj.optString("georgian", ""),
                        transcription = obj.optString("transcription", ""),
                        russian = obj.optString("russian", ""),
                        grammaticalNote = obj.optString("grammaticalNote", ""),
                        isEncountered = obj.optBoolean("isEncountered", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getEncounteredSubForms(): List<WordSubForm> {
        return getSubForms().filter { it.isEncountered }
    }

    fun withFormEncountered(formGeorgian: String): WordEntity {
        val currentForms = getSubForms()
        var changed = false
        val updated = currentForms.map { form ->
            if (form.georgian == formGeorgian && !form.isEncountered) {
                changed = true
                form.copy(isEncountered = true)
            } else {
                form
            }
        }
        if (!changed) return this
        val arr = JSONArray()
        for (item in updated) {
            val obj = JSONObject().apply {
                put("georgian", item.georgian)
                put("transcription", item.transcription)
                put("russian", item.russian)
                put("grammaticalNote", item.grammaticalNote)
                put("isEncountered", item.isEncountered)
            }
            arr.put(obj)
        }
        return copy(subFormsJson = arr.toString())
    }

    fun getSynonymList(): List<String> {
        val list = mutableListOf<String>()
        if (synonyms.isNotBlank()) {
            list.addAll(synonyms.split(',', '/').map { it.trim() }.filter { it.isNotEmpty() })
        }
        return list
    }

    fun getProgressPercent(): Int {
        return when {
            learned -> 100
            repetitions >= 5 -> 95
            repetitions == 4 -> 80
            repetitions == 3 -> 65
            repetitions == 2 -> 45
            repetitions == 1 -> 25
            else -> 0
        }
    }
}
