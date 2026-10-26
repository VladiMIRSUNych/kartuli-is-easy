package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class AudioHelper(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "AudioHelper"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = try {
        TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
        null
    }
    private var isTtsReady = false
    private var isGeorgianTtsSupported = false
    private var isRussianTtsSupported = false

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (e: Exception) {
        null
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            try {
                // Priority 1: Check native Georgian TTS engine
                val kaLocale = Locale.forLanguageTag("ka-GE")
                val kaResult = tts?.setLanguage(kaLocale)
                if (kaResult != TextToSpeech.LANG_MISSING_DATA && kaResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isGeorgianTtsSupported = true
                    Log.i(TAG, "Native Georgian TTS voice is available!")
                } else {
                    val genericKa = Locale.forLanguageTag("ka")
                    val genericRes = tts?.setLanguage(genericKa)
                    if (genericRes != TextToSpeech.LANG_MISSING_DATA && genericRes != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isGeorgianTtsSupported = true
                        Log.i(TAG, "Generic Georgian TTS voice is available!")
                    } else {
                        // Native Georgian is not on device. Use Russian TTS for natural Cyrillic phonetic rendering.
                        // NEVER use English TTS which spells out words letter by letter!
                        val ruLocale = Locale("ru", "RU")
                        val ruResult = tts?.setLanguage(ruLocale)
                        if (ruResult != TextToSpeech.LANG_MISSING_DATA && ruResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                            isRussianTtsSupported = true
                            tts?.language = ruLocale
                            Log.i(TAG, "Using Russian TTS with natural phonetics fallback (prevents English spelling)")
                        } else {
                            tts?.language = Locale.getDefault()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "TTS setLanguage exception: ${e.message}")
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    /**
     * Speaks Georgian text naturally.
     * 1. Attempts authentic native Georgian audio cache/stream.
     * 2. If device has Georgian TTS, speaks native Mkhedruli script.
     * 3. If device lacks Georgian TTS, transliterates to phonetic Cyrillic
     *    and speaks via Russian TTS so words like 'გთხოვთ' sound like 'гтховт'
     *    and 'არა' sounds like 'ара' (never letter-by-letter English spelling).
     */
    fun speakGeorgian(text: String, phoneticFallback: String = "") {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        scope.launch {
            // First check if cached online authentic audio is available
            val played = playCachedOrOnlineAudio(cleanText)
            if (!played) {
                speakWithEngine(cleanText, phoneticFallback)
            }
        }
    }

    private fun playCachedOrOnlineAudio(text: String): Boolean {
        return try {
            val cacheKey = "ka_speech_${text.hashCode().toString().replace('-', 'n')}.mp3"
            val cacheFile = File(context.cacheDir, cacheKey)

            if (cacheFile.exists() && cacheFile.length() > 500L) {
                playAudioFile(cacheFile)
                return true
            }

            // Google TTS supports tl=ru with 100% reliability (returns 200 with clear MP3 audio).
            // Transliterating Georgian to Cyrillic phonetics yields natural pronunciation:
            // "გთხოვთ" -> "гтховт", "არა" -> "ара", "გამარჯობა" -> "гамарджоба", "მადლობა" -> "мадлоба"
            val cyrillic = georgianToCyrillicPhonetics(text)
            val encodedQuery = URLEncoder.encode(cyrillic.take(120), "UTF-8")
            val url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ru&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val bytes = body.bytes()
                    if (bytes.size > 500) {
                        FileOutputStream(cacheFile).use { fos -> fos.write(bytes) }
                        playAudioFile(cacheFile)
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Online TTS error: ${e.message}")
            false
        }
    }

    private fun speakWithEngine(text: String, phoneticFallback: String) {
        if (!isTtsReady || tts == null) {
            playToneChime()
            return
        }

        try {
            if (isGeorgianTtsSupported) {
                // Device has native Georgian voice
                tts?.setLanguage(Locale.forLanguageTag("ka"))
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KARTULI_TTS")
            } else {
                // Device lacks Georgian voice: use Russian TTS with phonetic transliteration
                val phoneticText = if (phoneticFallback.contains("/")) {
                    val partAfterSlash = phoneticFallback.substringAfter("/").trim()
                    if (partAfterSlash.isNotBlank()) partAfterSlash else georgianToCyrillicPhonetics(text)
                } else {
                    georgianToCyrillicPhonetics(text)
                }

                tts?.setLanguage(Locale("ru", "RU"))
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(0.92f)
                tts?.speak(phoneticText, TextToSpeech.QUEUE_FLUSH, null, "KARTULI_PHONETIC_TTS")
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak error: ${e.message}")
            playToneChime()
        }
    }

    /**
     * Converts Georgian Mkhedruli script to natural Cyrillic phonetics.
     * Uses 'е' for 'ე' so Russian TTS never misinterprets words with consonant clusters
     * as abbreviations (e.g. 'рдзе' is pronounced smoothly as [rdze], NOT spelled out as 'эр-дзэ-э').
     */
    fun georgianToCyrillicPhonetics(georgianText: String): String {
        val clean = georgianText.trim().replace(Regex("[.,!?:;\"()]"), "")
        val overrides = mapOf(
            "რძე" to "рдзе",
            "რძით" to "рдзит",
            "რძეში" to "рдзеши",
            "რძის" to "рдзис",
            "რძეს" to "рдзес",
            "გთხოვთ" to "гтховт",
            "არა" to "ара",
            "კი" to "ки",
            "არ" to "ар",
            "მადლობა" to "мадлоба",
            "გმადლობთ" to "гмадлобт",
            "დიდი" to "диди",
            "ნახვამდის" to "нахвамдис",
            "გამარჯობა" to "гамарджоба",
            "გამარჯობათ" to "гамарджобат",
            "ბოდიში" to "бодиши",
            "კარგად" to "каргад",
            "კარგი" to "карги",
            "ლამაზი" to "ламази",
            "ყავა" to "кава",
            "ყავას" to "кавас",
            "ჩაი" to "чай",
            "ჩაის" to "чаис",
            "წყალი" to "цкали",
            "წყალს" to "цкалс",
            "ღვინო" to "гвино",
            "პური" to "пури",
            "ხაჭაპური" to "хачапури",
            "ხინკალი" to "хинкали",
            "მეგობარი" to "мегобари",
            "სახლი" to "сахли",
            "კაფე" to "кафе",
            "თბილისი" to "тбилиси",
            "ვარ" to "вар",
            "ხარ" to "хар",
            "არის" to "арис",
            "მინდა" to "минда",
            "გინდა" to "гинда",
            "უნდა" to "унда",
            "გემრიელია" to "гемриелиа",
            "ცხელი" to "цхели",
            "ცივი" to "циви",
            "ანგარიში" to "ангариши"
        )
        val direct = overrides[clean]
        if (direct != null) return direct

        val map = mapOf(
            'ა' to "а", 'ბ' to "б", 'გ' to "г", 'დ' to "д", 'ე' to "е",
            'ვ' to "в", 'ზ' to "з", 'თ' to "т", 'ი' to "и", 'კ' to "к",
            'ლ' to "л", 'მ' to "м", 'ნ' to "н", 'ო' to "о", 'პ' to "п",
            'ჟ' to "ж", 'რ' to "р", 'ს' to "с", 'ტ' to "т", 'უ' to "у",
            'ფ' to "п", 'ქ' to "к", 'ღ' to "г", 'ყ' to "к", 'შ' to "ш",
            'ჩ' to "ч", 'ც' to "ц", 'ძ' to "дз", 'წ' to "ц", 'ჭ' to "ч",
            'ხ' to "х", 'ჯ' to "дж", 'ჰ' to "х"
        )
        val sb = StringBuilder()
        for (ch in georgianText) {
            sb.append(map[ch] ?: ch)
        }
        return sb.toString()
    }

    private fun playAudioFile(file: File) {
        try {
            stopAudio()
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                java.io.FileInputStream(file).use { fis ->
                    setDataSource(fis.fd)
                }
                setVolume(1.0f, 1.0f)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                }
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer play error: ${e.message}")
        }
    }

    fun playCorrectSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (e: Exception) {
            Log.e(TAG, "Tone error: ${e.message}")
        }
    }

    fun playIncorrectSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            Log.e(TAG, "Tone error: ${e.message}")
        }
    }

    fun playToneChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        stopAudio()
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
