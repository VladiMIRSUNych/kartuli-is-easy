package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateNewWord(userInput: String): Result<WordEntity> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback if API key is not yet configured
            return@withContext Result.success(createLocalFallbackWord(userInput))
        }

        val prompt = """
            You are an expert Georgian language teacher. The user wants to add a word or phrase to their Georgian vocabulary deck: "$userInput".
            Return ONLY a valid JSON object (no markdown fences, no formatting) with these exact keys:
            {
              "georgian": "ქართული სიტყვა (Georgian in Mkhedruli script)",
              "transcription": "latin transcription / русская транскрипция",
              "russian": "перевод на русский",
              "category": "one of: greetings, food, city, market, chat, home, custom",
              "emoji": "a relevant single emoji icon",
              "exampleGeorgian": "a simple, highly useful everyday spoken Georgian sentence containing the word",
              "exampleRussian": "Russian translation of the example sentence"
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            val jsonClean = cleanJsonOutput(responseText)
            val obj = JSONObject(jsonClean)

            val word = WordEntity(
                georgian = obj.optString("georgian", userInput),
                transcription = obj.optString("transcription", "kartuli"),
                russian = obj.optString("russian", userInput),
                category = obj.optString("category", "custom"),
                emoji = obj.optString("emoji", "🇬🇪"),
                exampleGeorgian = obj.optString("exampleGeorgian", ""),
                exampleRussian = obj.optString("exampleRussian", ""),
                isCustom = true
            )
            Result.success(word)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini word generation error: ${e.message}", e)
            Result.success(createLocalFallbackWord(userInput))
        }
    }

    suspend fun generateSentences(georgianWord: String, russianWord: String): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(listOf(
                Pair("$georgianWord ძალიან მომწონს.", "Мне очень нравится $russianWord."),
                Pair("ეს არის ჩემი $georgianWord.", "Это мой $russianWord."),
                Pair("სად შეიძლება ვიყიდო $georgianWord?", "Где можно купить $russianWord?")
            ))
        }

        val prompt = """
            Create 3 practical, natural everyday spoken Georgian sentences using the word:
            Georgian: "$georgianWord" (Russian meaning: "$russianWord").
            Focus on daily life in Georgia (cafe, taxi, street, market, friends).
            Return ONLY a valid JSON array of objects with keys "georgian" and "russian":
            [
              {"georgian": "...", "russian": "..."},
              {"georgian": "...", "russian": "..."},
              {"georgian": "...", "russian": "..."}
            ]
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            val jsonClean = cleanJsonOutput(responseText)
            val arr = JSONArray(jsonClean)
            val list = mutableListOf<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                list.add(Pair(item.getString("georgian"), item.getString("russian")))
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini sentence error: ${e.message}", e)
            Result.success(listOf(
                Pair("$georgianWord ძალიან მომწონს.", "Мне очень нравится $russianWord."),
                Pair("აქ არის $georgianWord.", "Здесь $russianWord."),
                Pair("მომეცით $georgianWord, გთხოვთ.", "Дайте мне $russianWord, пожалуйста.")
            ))
        }
    }

    suspend fun explainGrammar(phrase: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success("В грузинском языке ударение обычно падает на первый слог (в коротких словах) или третий от конца. Буквы читаются всегда одинаково, как пишутся! Нет категорий грамматического рода (он/она - одинаково 'ის').")
        }

        val prompt = """
            Explain the grammar or cultural nuance of the Georgian phrase "$phrase" for a beginner learning Georgian (in Russian).
            Keep it clear, friendly, and concise (under 3 sentences) in Duolingo style with practical tips on pronunciation or usage.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            Result.success(responseText.trim())
        } catch (e: Exception) {
            Result.success("В грузинском языке нет грамматического рода, а буквы читаются ровно так, как пишутся. Смело произносите каждый звук чётко!")
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.body?.string()}")
            }
            val bodyString = response.body?.string() ?: throw Exception("Empty response body")
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: throw Exception("No candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    suspend fun generateNewUnit(theme: String, orderIndex: Int): Result<GeneratedUnit> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(createFallbackUnit(theme, orderIndex))
        }

        val prompt = """
            Create a complete Georgian language learning unit for beginners about: "$theme".
            Focus on practical everyday spoken Georgian phrases used in Georgia.
            Return ONLY a valid JSON object (no markdown, no backticks) with this structure:
            {
              "title": "Short Russian Title (e.g. В аптеке)",
              "emoji": "1 matching emoji (e.g. 💊)",
              "description": "Short description of everyday situations",
              "words": [
                {
                  "georgian": "Georgian word in Mkhedruli",
                  "transcription": "latin / русская транскрипция",
                  "russian": "русский перевод",
                  "emoji": "emoji",
                  "exampleGeorgian": "practical Georgian sentence",
                  "exampleRussian": "Russian translation"
                }
              ]
            }
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt, apiKey)
            val jsonClean = cleanJsonOutput(responseText)
            val obj = JSONObject(jsonClean)
            val unitId = "ai_" + System.currentTimeMillis()
            val unit = com.example.data.UnitEntity(
                id = unitId,
                title = obj.optString("title", theme),
                emoji = obj.optString("emoji", "✨"),
                description = obj.optString("description", "Тематический модуль от ИИ"),
                orderIndex = orderIndex,
                isCustom = true
            )
            val wordsJson = obj.optJSONArray("words") ?: JSONArray()
            val wordsList = mutableListOf<WordEntity>()
            for (i in 0 until wordsJson.length()) {
                val w = wordsJson.getJSONObject(i)
                wordsList.add(
                    WordEntity(
                        georgian = w.getString("georgian"),
                        transcription = w.getString("transcription"),
                        russian = w.getString("russian"),
                        category = unitId,
                        emoji = w.optString("emoji", unit.emoji),
                        exampleGeorgian = w.optString("exampleGeorgian", ""),
                        exampleRussian = w.optString("exampleRussian", ""),
                        isCustom = true
                    )
                )
            }
            if (wordsList.isEmpty()) {
                Result.success(createFallbackUnit(theme, orderIndex))
            } else {
                Result.success(GeneratedUnit(unit, wordsList))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unit generation error: ${e.message}", e)
            Result.success(createFallbackUnit(theme, orderIndex))
        }
    }

    private fun createFallbackUnit(theme: String, orderIndex: Int): GeneratedUnit {
        val unitId = "ai_" + System.currentTimeMillis()
        val unit = com.example.data.UnitEntity(
            id = unitId,
            title = theme.replaceFirstChar { it.uppercase() },
            emoji = "✨",
            description = "Модуль по теме «$theme»",
            orderIndex = orderIndex,
            isCustom = true
        )
        val words = listOf(
            WordEntity(
                georgian = "ახალი",
                transcription = "axali / ахали",
                russian = "новый ($theme)",
                category = unitId,
                emoji = "✨",
                exampleGeorgian = "ეს არის ახალი თემა.",
                exampleRussian = "Это новая тема.",
                isCustom = true
            ),
            WordEntity(
                georgian = "კარგი",
                transcription = "kargi / карги",
                russian = "хороший ($theme)",
                category = unitId,
                emoji = "👍",
                exampleGeorgian = "ძალიან კარგია!",
                exampleRussian = "Очень хорошо!",
                isCustom = true
            ),
            WordEntity(
                georgian = "მინდა",
                transcription = "minda / минда",
                russian = "я хочу ($theme)",
                category = unitId,
                emoji = "🎯",
                exampleGeorgian = "მე ეს მინდა.",
                exampleRussian = "Я хочу это.",
                isCustom = true
            ),
            WordEntity(
                georgian = "სად არის",
                transcription = "sad aris / сад арис",
                russian = "где находится ($theme)",
                category = unitId,
                emoji = "📍",
                exampleGeorgian = "სად არის ეს?",
                exampleRussian = "Где это находится?",
                isCustom = true
            )
        )
        return GeneratedUnit(unit, words)
    }

    private fun cleanJsonOutput(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    private fun createLocalFallbackWord(input: String): WordEntity {
        val isGeorgian = input.any { it in '\u10A0'..'\u10FF' }
        return if (isGeorgian) {
            WordEntity(
                georgian = input,
                transcription = "$input (грузинское слово)",
                russian = "пользовательское слово",
                category = "custom",
                emoji = "🇬🇪",
                exampleGeorgian = "$input არის ძალიან კარგი.",
                exampleRussian = "$input — это очень хорошо.",
                isCustom = true
            )
        } else {
            WordEntity(
                georgian = input,
                transcription = "$input",
                russian = input,
                category = "custom",
                emoji = "✨",
                exampleGeorgian = "$input მომწონს.",
                exampleRussian = "Мне нравится $input.",
                isCustom = true
            )
        }
    }
}

data class GeneratedUnit(
    val unit: com.example.data.UnitEntity,
    val words: List<WordEntity>
)

