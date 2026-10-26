package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String, Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var targetPhrase: String = ""
    private var targetTranscription: String = ""

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Не удалось распознать речь. Попробуйте еще раз."
                            SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудиозаписи."
                            SpeechRecognizer.ERROR_NETWORK -> "Сетевая ошибка распознавания."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Время ожидания речи истекло."
                            else -> "Попробуйте произнести еще раз."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull() ?: ""
                        val isAccurate = evaluatePronunciation(spoken, targetPhrase, targetTranscription)
                        onResult(spoken, isAccurate)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            speechRecognizer = null
        }
    }

    fun startListening(targetPhrase: String, targetTranscription: String) {
        this.targetPhrase = targetPhrase
        this.targetTranscription = targetTranscription

        if (speechRecognizer == null) {
            onError("Распознавание речи недоступно на данном устройстве.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ka-GE")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("ka-GE", "ru-RU", "en-US"))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Произнесите: $targetPhrase")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("SpeechHelper", "Start listening error: ${e.message}")
            onError("Не удалось запустить микрофон.")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Checks if spoken text is close enough to target Georgian text or its phonetic transcription
     */
    private fun evaluatePronunciation(spoken: String, target: String, transcription: String): Boolean {
        if (spoken.isBlank()) return false
        val s = spoken.lowercase().trim()
        val t = target.lowercase().trim()
        val tr = transcription.lowercase().trim()

        if (s == t || s.contains(t) || t.contains(s)) return true

        // Check latin/cyrillic transcriptions
        val parts = tr.split("/", ",", " ")
        for (part in parts) {
            val clean = part.trim()
            if (clean.length > 2 && (s.contains(clean) || clean.contains(s))) {
                return true
            }
        }

        // Levenshtein similarity on normalized text
        val distance = levenshteinDistance(s, t)
        val maxLen = maxOf(s.length, t.length)
        if (maxLen > 0 && (1.0 - distance.toDouble() / maxLen) >= 0.5) {
            return true
        }

        return false
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }
}
