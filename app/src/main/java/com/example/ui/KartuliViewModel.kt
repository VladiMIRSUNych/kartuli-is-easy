package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.AppDatabase
import com.example.data.InitialData
import com.example.data.StoryEntity
import com.example.data.UserStatsEntity
import com.example.data.WordEntity
import com.example.srs.SrsManager
import com.example.srs.SrsRating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// -------------------------------------------------------------------------------------------------
// EXERCISE TYPES (No flip cards; includes NewWordPresentation, Translation, Story, Bubbles)
// -------------------------------------------------------------------------------------------------
sealed class Exercise {
    // 1. New Word Presentation (explains meaning, transcription, audio, writing practice)
    data class NewWordPresentation(
        val word: WordEntity,
        val explanation: String
    ) : Exercise()

    // 2. Picture / Visual Choice
    data class PictureChoice(
        val question: String,
        val targetWord: WordEntity,
        val options: List<WordEntity>
    ) : Exercise()

    // 3. Input Russian translation (evaluated ONLY on "Проверить" click)
    data class InputRussian(
        val targetWord: WordEntity,
        val phrasePrompt: String = ""
    ) : Exercise()

    // 4. Input Georgian translation with native keyboard (evaluated ONLY on "Проверить" click)
    data class InputGeorgian(
        val targetWord: WordEntity,
        val promptRussian: String
    ) : Exercise()

    // 5. Listening (Listen to Georgian audio, choose correct Russian translation)
    data class Listening(
        val targetWord: WordEntity,
        val options: List<String>,
        val correctIndex: Int
    ) : Exercise()

    // 6. Sentence builder using word bubbles (Georgian -> Russian or Russian -> Georgian)
    data class BubbleSentence(
        val promptText: String,
        val promptLanguage: String, // "GE" or "RU"
        val expectedWords: List<String>,
        val scrambledBank: List<String>,
        val fullTranslation: String
    ) : Exercise()

    // 7. Matching pairs (4-5 pairs of Georgian & Russian tiles)
    data class MatchingPairs(
        val pairs: List<Pair<String, String>> // (Georgian, Russian)
    ) : Exercise()

    // 8. Story of the Day (Interactive reader with audio, tap-to-translate, checkpoints every 2-3 sentences)
    data class StoryOfToday(
        val storyId: String,
        val titleGeorgian: String,
        val titleRussian: String,
        val sentences: List<StorySentenceItem>,
        val wordDictionary: Map<String, String>,
        val checkpoints: List<StoryCheckpointItem>
    ) : Exercise()

    // 9. Speech / Pronunciation
    data class Pronunciation(
        val targetPhrase: String,
        val transcription: String,
        val translation: String
    ) : Exercise()
}

data class StorySentenceItem(
    val index: Int,
    val georgian: String,
    val russian: String,
    val audioPhonetic: String = ""
)

data class StoryCheckpointItem(
    val triggerAfterSentenceIndex: Int, // e.g. 1 (after 2 sentences), 3 (after 4 sentences)
    val questionPrompt: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val isBubbleAssembly: Boolean = false,
    val expectedWords: List<String> = emptyList(),
    val wordBank: List<String> = emptyList(),
    val explanation: String = ""
)

data class LessonState(
    val categoryId: String,
    val categoryTitle: String,
    val exercises: List<Exercise>,
    val currentIndex: Int = 0,
    val earnedXp: Int = 0,
    val isFinished: Boolean = false,
    val mistakeCount: Int = 0
)

enum class DailyLessonType {
    NEW_WORDS,
    REPETITION,
    STORY
}

data class DailyLessonInfo(
    val id: String,
    val type: DailyLessonType,
    val title: String,
    val subtitle: String,
    val durationText: String = "~10 минут",
    val exerciseCountText: String = "12 заданий",
    val iconEmoji: String = "📘",
    val isCompleted: Boolean = false
)

class KartuliViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val wordDao = db.wordDao()
    val userStatsDao = db.userStatsDao()
    val storyDao = db.storyDao()
    val unitDao = db.unitDao()

    val allUnits: StateFlow<List<com.example.data.UnitEntity>> = unitDao.getAllUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWords: StateFlow<List<WordEntity>> = wordDao.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = userStatsDao.getUserStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allStories: StateFlow<List<StoryEntity>> = storyDao.getAllStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _srsDueWords = MutableStateFlow<List<WordEntity>>(emptyList())
    val srsDueWords: StateFlow<List<WordEntity>> = _srsDueWords.asStateFlow()

    private val _activeLesson = MutableStateFlow<LessonState?>(null)
    val activeLesson: StateFlow<LessonState?> = _activeLesson.asStateFlow()

    // 3 Daily Lessons State
    private val _dailyLessons = MutableStateFlow<List<DailyLessonInfo>>(emptyList())
    val dailyLessons: StateFlow<List<DailyLessonInfo>> = _dailyLessons.asStateFlow()

    private var currentLessonBatchSeed = 0

    // AI Generation states
    private val _aiGenerationLoading = MutableStateFlow(false)
    val aiGenerationLoading: StateFlow<Boolean> = _aiGenerationLoading.asStateFlow()

    private val _grammarExplanation = MutableStateFlow<String?>(null)
    val grammarExplanation: StateFlow<String?> = _grammarExplanation.asStateFlow()

    private val _aiGeneratedSentences = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiGeneratedSentences: StateFlow<List<Pair<String, String>>> = _aiGeneratedSentences.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userStatsDao.getUserStatsOnce() == null) {
                    userStatsDao.insertUserStats(UserStatsEntity())
                }
                if (wordDao.getCount() == 0) {
                    wordDao.insertWords(InitialData.DEFAULT_WORDS)
                }
                if (storyDao.getCount() == 0) {
                    storyDao.insertStories(InitialData.DEFAULT_STORIES)
                }
                if (unitDao.getCount() == 0) {
                    unitDao.insertUnits(InitialData.DEFAULT_UNITS)
                }
            } catch (e: Exception) {
                // Ignore initialization errors
            }
            refreshSrsDueWords()
            generateDailyLessons(forceRefresh = false)
        }
    }

    fun refreshSrsDueWords() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                wordDao.getWordsDueForReview(now).collect { due ->
                    _srsDueWords.value = due
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Generates or refreshes the 3 daily lessons (total ~30 minutes of study):
     * 1. Новые слова (~10 мин)
     * 2. Интервальное повторение (~10 мин)
     * 3. История дня (~10 мин)
     */
    fun generateDailyLessons(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            currentLessonBatchSeed++
        }

        val lessons = listOf(
            DailyLessonInfo(
                id = "daily_new_words_$currentLessonBatchSeed",
                type = DailyLessonType.NEW_WORDS,
                title = "Урок 1: Новые слова",
                subtitle = "Подробное объяснение, произношение, перевод и первичное письмо",
                durationText = "~10 минут",
                exerciseCountText = "12 заданий",
                iconEmoji = "📘",
                isCompleted = false
            ),
            DailyLessonInfo(
                id = "daily_repetition_$currentLessonBatchSeed",
                type = DailyLessonType.REPETITION,
                title = "Урок 2: Повторение (SRS)",
                subtitle = "Интервальное повторение: активный перевод и составление фраз",
                durationText = "~10 минут",
                exerciseCountText = "14 заданий",
                iconEmoji = "🔄",
                isCompleted = false
            ),
            DailyLessonInfo(
                id = "daily_story_$currentLessonBatchSeed",
                type = DailyLessonType.STORY,
                title = "Урок 3: История дня",
                subtitle = "Интерактивный рассказ из известных слов с озвучкой и вопросами",
                durationText = "~10 минут",
                exerciseCountText = "7 предложений + задания",
                iconEmoji = "📖",
                isCompleted = false
            )
        )
        _dailyLessons.value = lessons
    }

    /**
     * Starts one of the 3 daily lessons based on user selection
     */
    fun startDailyLesson(dailyLesson: DailyLessonInfo) {
        when (dailyLesson.type) {
            DailyLessonType.NEW_WORDS -> startNewWordsLesson(dailyLesson.id, dailyLesson.title)
            DailyLessonType.REPETITION -> startRepetitionLesson(dailyLesson.id, dailyLesson.title)
            DailyLessonType.STORY -> startDailyStoryLesson(dailyLesson.id, dailyLesson.title)
        }
    }

    /**
     * LESSON 1: Новые слова (~10 минут)
     * Introduces 3-4 new words. Every word is EXPLAINED first:
     * transcription, translation, audio, usage context, and user writes it before testing!
     */
    private fun startNewWordsLesson(lessonId: String, title: String) {
        viewModelScope.launch {
            val wordsList = allWords.value.ifEmpty { InitialData.DEFAULT_WORDS }

            // Find unlearned words (repetitions == 0) or offset by seed
            val unlearned = wordsList.filter { !it.learned || it.repetitions == 0 }
            val pool = if (unlearned.size >= 4) unlearned else wordsList
            val startIdx = (currentLessonBatchSeed * 3) % pool.size
            val selectedWords = (pool.drop(startIdx) + pool).distinctBy { it.id }.take(3)

            val distractors = wordsList.filter { it.id !in selectedWords.map { w -> w.id } }.shuffled()
            val exercises = mutableListOf<Exercise>()

            // Context explanations map for common Georgian words
            val explanationMap = mapOf(
                "გთხოვთ" to "Вежливая форма просьбы. Употребляется при заказе блюд в кафе, покупке в магазине или когда вежливо просите об услуге ('ერთი ყავა, გთხოვთ' = Один кофе, пожалуйста).",
                "არა" to "Базовое отрицание ('нет'). Произносится твёрдо и чётко: [ара́]. Не путать с отрицательной частицей 'ар' ('არ მინდა' = не хочу).",
                "გამარჯობა" to "Главное приветствие в Грузии. Буквально переводится как 'победы тебе!'. Ответ на него — 'გაგიმარჯოს' (гагимарджос).",
                "მადლობა" to "Слово благодарности ('спасибо'). Для усиления говорят 'დიდი მადლობა' (диди мадлоба = большое спасибо).",
                "ნახვამდის" to "Прощание ('до свидания', 'до встречи'). Буквально означает 'до увиденного'.",
                "კი" to "Разговорное согласие ('да'). Для более официального ответа используется 'დიახ' (диах).",
                "ბოდიში" to "Извинение ('простите', 'извините'). Используется, чтобы привлечь внимание или извиниться за оплошность.",
                "ხაჭაპური" to "Традиционная грузинская лепёшка с сыром. Состоит из слов 'хачо' (творог/сыр) и 'пури' (хлеб).",
                "ხინკალი" to "Знаменитое грузинское блюдо с мясом и ароматным бульоном. Едят руками, оставляя хвостик ('кучи').",
                "წყალი" to "Вода. Одно из важнейших бытовых слов. Часто используется при заказе: 'ერთი ბოთლი წყალი, გთხოვთ'."
            )

            for (word in selectedWords) {
                val expl = explanationMap[word.georgian]
                    ?: "Слово активно используется в повседневной речи в категории '${word.category}'. Пример: ${word.exampleGeorgian} (${word.exampleRussian})"

                // 1. Detailed explanation + audio + writing step (Requirement #3)
                exercises.add(
                    Exercise.NewWordPresentation(
                        word = word,
                        explanation = expl
                    )
                )

                // 2. Listening verification
                val listenOptions = (listOf(word.russian) + distractors.take(3).map { it.russian }).shuffled()
                exercises.add(
                    Exercise.Listening(
                        targetWord = word,
                        options = listenOptions,
                        correctIndex = listenOptions.indexOf(word.russian)
                    )
                )

                // 3. Sentence assembly with the new word (Requirement #8)
                if (word.exampleGeorgian.isNotBlank() && word.exampleRussian.isNotBlank()) {
                    val geTokens = word.exampleGeorgian
                        .replace(".", "").replace("!", "").replace("?", "").replace(",", "")
                        .split(" ").filter { it.isNotBlank() }
                    val bankTokens = (geTokens + listOf("და", "აქ", "არის").filter { !geTokens.contains(it) }).shuffled()
                    exercises.add(
                        Exercise.BubbleSentence(
                            promptText = word.exampleRussian,
                            promptLanguage = "RU",
                            expectedWords = geTokens,
                            scrambledBank = bankTokens,
                            fullTranslation = word.exampleGeorgian
                        )
                    )
                }

                // 4. Translate into Russian (Requirement #9: verified ONLY on "Проверить" click)
                exercises.add(
                    Exercise.InputRussian(
                        targetWord = word,
                        phrasePrompt = word.georgian
                    )
                )
            }

            // Summary matching exercise
            val pairs = selectedWords.map { Pair(it.georgian, it.russian) }
            exercises.add(Exercise.MatchingPairs(pairs))

            _activeLesson.value = LessonState(
                categoryId = lessonId,
                categoryTitle = title,
                exercises = exercises,
                currentIndex = 0,
                earnedXp = 0,
                isFinished = false
            )
        }
    }

    /**
     * LESSON 2: Интервальное повторение (~10 минут)
     * Spaced repetition with heavy emphasis on translation and phrase assembly (Requirement #2, #8)
     */
    private fun startRepetitionLesson(lessonId: String, title: String) {
        viewModelScope.launch {
            val wordsList = allWords.value.ifEmpty { InitialData.DEFAULT_WORDS }

            // Pull due words from SRS, or words previously learned
            val due = srsDueWords.value
            val candidateWords = if (due.size >= 4) due else wordsList.filter { it.learned || it.repetitions > 0 }.ifEmpty { wordsList }
            val pool = candidateWords.shuffled().take(6)
            val distractors = wordsList.filter { it.id !in pool.map { w -> w.id } }.shuffled()

            val exercises = mutableListOf<Exercise>()

            // 1. Matching tiles for warmup
            val warmupPairs = pool.take(4).map { Pair(it.georgian, it.russian) }
            exercises.add(Exercise.MatchingPairs(warmupPairs))

            // 2. Active translation into Russian (verified ONLY on "Проверить" click)
            for (word in pool.take(3)) {
                exercises.add(Exercise.InputRussian(targetWord = word, phrasePrompt = word.georgian))
            }

            // 3. Sentence building from bubbles (Requirement #8: lots of phrase building)
            for (word in pool.filter { it.exampleGeorgian.isNotBlank() }.take(3)) {
                val tokens = word.exampleGeorgian
                    .replace(".", "").replace("!", "").replace("?", "").replace(",", "")
                    .split(" ").filter { it.isNotBlank() }
                val bank = (tokens + listOf("გთხოვთ", "არის", "ძალიან").filter { !tokens.contains(it) }).shuffled()
                exercises.add(
                    Exercise.BubbleSentence(
                        promptText = word.exampleRussian,
                        promptLanguage = "RU",
                        expectedWords = tokens,
                        scrambledBank = bank,
                        fullTranslation = word.exampleGeorgian
                    )
                )
            }

            // 4. Translate Russian to Georgian using native keyboard (Requirement #7, #9)
            for (word in pool.takeLast(2)) {
                exercises.add(
                    Exercise.InputGeorgian(
                        targetWord = word,
                        promptRussian = word.russian
                    )
                )
            }

            // 5. Listening comprehension
            for (word in pool.take(2)) {
                val options = (listOf(word.russian) + distractors.take(3).map { it.russian }).shuffled()
                exercises.add(
                    Exercise.Listening(
                        targetWord = word,
                        options = options,
                        correctIndex = options.indexOf(word.russian)
                    )
                )
            }

            _activeLesson.value = LessonState(
                categoryId = lessonId,
                categoryTitle = title,
                exercises = exercises,
                currentIndex = 0,
                earnedXp = 0,
                isFinished = false
            )
        }
    }

    /**
     * LESSON 3: История дня (~10 минут) (Requirement #5)
     * Sentence-by-sentence reading with audio, every word clickable for translation (logs lookup to SRS),
     * and checkpoint challenges after every 2-3 sentences.
     * All words and sentences are drawn STRICTLY from the dictionary!
     */
    private fun startDailyStoryLesson(lessonId: String, title: String) {
        viewModelScope.launch {
            val wordsList = allWords.value.ifEmpty { InitialData.DEFAULT_WORDS }
            val storiesCatalog = listOf(
                createStoryCafeTbilisi(wordsList),
                createStoryCityWalk(wordsList),
                createStoryFriendlyMeeting(wordsList)
            )

            val storyIndex = currentLessonBatchSeed % storiesCatalog.size
            val storyExercise = storiesCatalog[storyIndex]

            // Mark subforms used in this story as encountered in the dictionary
            unlockStorySubforms(storyExercise)

            _activeLesson.value = LessonState(
                categoryId = lessonId,
                categoryTitle = title,
                exercises = listOf(storyExercise),
                currentIndex = 0,
                earnedXp = 0,
                isFinished = false
            )
        }
    }

    private fun getDictionaryForWords(wordsList: List<WordEntity>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (w in wordsList) {
            map[w.georgian] = "${w.russian} (${w.getPartOfSpeechEnum().labelRu})"
            for (sub in w.getSubForms()) {
                map[sub.georgian] = "${sub.russian} [${sub.grammaticalNote}] (словоформа от '${w.georgian}')"
            }
        }
        return map
    }

    private fun unlockStorySubforms(story: Exercise.StoryOfToday) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wordsInDb = wordDao.getAllWordsOnce()
                for (sentence in story.sentences) {
                    val tokens = sentence.georgian.replace(Regex("[.,!?:;\"()]"), "").split(" ")
                    for (token in tokens) {
                        val clean = token.trim()
                        if (clean.isBlank()) continue
                        val parentWord = wordsInDb.find { it.getSubForms().any { f -> f.georgian == clean } }
                        if (parentWord != null) {
                            val updated = parentWord.withFormEncountered(clean)
                            wordDao.updateWord(updated)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun createStoryCafeTbilisi(wordsList: List<WordEntity>): Exercise.StoryOfToday {
        val sentences = listOf(
            StorySentenceItem(0, "გამარჯობა! მე ვარ თბილისში.", "Здравствуйте! Я в Тбилиси."),
            StorySentenceItem(1, "დღეს მე მივდივარ კაფეში.", "Сегодня я иду в кафе."),
            StorySentenceItem(2, "აქ არის ჩემი მეგობარი.", "Здесь мой друг."),
            StorySentenceItem(3, "მე მინდა ცხელი ჩაი რძით და ხაჭაპური.", "Я хочу горячий чай с молоком и хачапури."),
            StorySentenceItem(4, "ჩემი მეგობარი სვამს ყავას.", "Мой друг пьёт кофе."),
            StorySentenceItem(5, "ეს კერძი ძალიან გემრიელია!", "Это блюдо очень вкусное!"),
            StorySentenceItem(6, "ანგარიში მოიტანეთ, გთხოვთ.", "Принесите счёт, пожалуйста."),
            StorySentenceItem(7, "დიდი მადლობა და ნახვამდის!", "Большое спасибо и до свидания!")
        )

        val dict = getDictionaryForWords(wordsList).toMutableMap()
        dict["მოიტანეთ"] = "принесите (повелительная форма)"

        val checkpoints = listOf(
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 2,
                questionPrompt = "Куда пришёл автор истории?",
                options = listOf("В кафе в Тбилиси (კაფეში თბილისში)", "Домой спать", "На вокзал"),
                correctIndex = 0,
                explanation = "В тексте: 'მე ვარ თბილისში. დღეს მე მივდივარ კაფეში'."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 4,
                questionPrompt = "Соберите заказ автора (чай с молоком):",
                isBubbleAssembly = true,
                expectedWords = listOf("ცხელი", "ჩაი", "რძით", "და", "ხაჭაპური"),
                wordBank = listOf("ცხელი", "ჩაი", "რძით", "და", "ხაჭაპური", "ღვინო", "არა"),
                explanation = "В тексте: 'მე მინდა ცხელი ჩაი რძით და ხაჭაპური' (Я хочу горячий чай с молоком и хачапури)."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 6,
                questionPrompt = "Что попросил автор в конце трапезы?",
                options = listOf("Принести счёт (ანგარიში)", "Ещё один кофе", "Холодную воду"),
                correctIndex = 0,
                explanation = "В тексте: 'ანგარიში მოიტანეთ, გთხოვთ' (Принесите счёт, пожалуйста)."
            )
        )

        return Exercise.StoryOfToday(
            storyId = "story_cafe_1",
            titleGeorgian = "კაფეში თბილისში",
            titleRussian = "В кафе в Тбилиси",
            sentences = sentences,
            wordDictionary = dict,
            checkpoints = checkpoints
        )
    }

    private fun createStoryCityWalk(wordsList: List<WordEntity>): Exercise.StoryOfToday {
        val sentences = listOf(
            StorySentenceItem(0, "გამარჯობა! დღეს ძალიან კარგი დღეა.", "Здравствуйте! Сегодня очень хороший день."),
            StorySentenceItem(1, "თბილისი არის ძალიან ლამაზი ქალაქი.", "Тбилиси — очень красивый город."),
            StorySentenceItem(2, "აქ არის ჩემი სახლი და ჩემი მეგობარი.", "Здесь мой дом и мой друг."),
            StorySentenceItem(3, "ჩვენ მივდივართ კაფეში.", "Мы идём в кафе."),
            StorySentenceItem(4, "მე მინდა ცივი წყალი, ჩემს მეგობარს უნდა ყავა.", "Я хочу холодную воду, а мой друг хочет кофе."),
            StorySentenceItem(5, "ეს ყავა რძით არის ძალიან გემრიელი.", "Этот кофе с молоком очень вкусный."),
            StorySentenceItem(6, "დიდი მადლობა, ნახვამდის და კარგად!", "Большое спасибо, до свидания и всего хорошего!")
        )

        val dict = getDictionaryForWords(wordsList).toMutableMap()
        dict["დღეა"] = "день (со связкой -а)"
        dict["ქალაქია"] = "город (со связкой -а)"

        val checkpoints = listOf(
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 2,
                questionPrompt = "Каким автор описывает Тбилиси?",
                options = listOf("Очень красивый город (ძალიან ლამაზი)", "Маленький и холодный", "Шумный"),
                correctIndex = 0,
                explanation = "В тексте: 'თბილისი არის ძალიან ლამაზი ქალაქი'."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 4,
                questionPrompt = "Что захотел выпить автор?",
                options = listOf("Холодную воду (ცივი წყალი)", "Горячий чай", "Красное вино"),
                correctIndex = 0,
                explanation = "В тексте: 'მე მინდა ცივი წყალი'."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 5,
                questionPrompt = "Соберите фразу о кофе с молоком:",
                isBubbleAssembly = true,
                expectedWords = listOf("ყავა", "რძით", "ძალიან", "გემრიელია"),
                wordBank = listOf("ყავა", "რძით", "ძალიან", "გემრიელია", "ცუდი", "არა"),
                explanation = "В тексте: 'ყავა რძით არის ძალიან გემრიელი'."
            )
        )

        return Exercise.StoryOfToday(
            storyId = "story_walk_1",
            titleGeorgian = "გასეირნება თბილისში",
            titleRussian = "Прогулка по Тбилиси",
            sentences = sentences,
            wordDictionary = dict,
            checkpoints = checkpoints
        )
    }

    private fun createStoryFriendlyMeeting(wordsList: List<WordEntity>): Exercise.StoryOfToday {
        val sentences = listOf(
            StorySentenceItem(0, "გამარჯობა! როგორ ხარ?", "Привет! Как дела?"),
            StorySentenceItem(1, "მე ვარ კარგად, დიდი მადლობა!", "У меня всё хорошо, большое спасибо!"),
            StorySentenceItem(2, "შენ სად მიდიხარ ახლა?", "Ты куда идёшь сейчас?"),
            StorySentenceItem(3, "მე მივდივარ კაფეში, წავიდეთ ერთად!", "Я иду в кафе, пойдём вместе!"),
            StorySentenceItem(4, "კი, სიამოვნებით! მე მინდა ცხელი ჩაი რძით.", "Да, с удовольствием! Я хочу горячий чай с молоком."),
            StorySentenceItem(5, "ჩვენ ვჭამთ ხაჭაპურს და ვლაპარაკობთ.", "Мы едим хачапури и разговариваем."),
            StorySentenceItem(6, "ნახვამდის, მეგობარო, ხვალამდე!", "До свидания, друг, до завтра!")
        )

        val dict = getDictionaryForWords(wordsList).toMutableMap()
        dict["ერთად"] = "вместе"
        dict["მეგობარო"] = "друг (звательная форма)"
        dict["ხვალამდე"] = "до завтра"

        val checkpoints = listOf(
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 1,
                questionPrompt = "Как ответил собеседник на вопрос 'როგორ ხარ'?",
                options = listOf("Всё хорошо, большое спасибо (კარგად)", "Плохо", "Не знаю"),
                correctIndex = 0,
                explanation = "В тексте: 'მე ვარ კარგად, დიდი მადლობა!'."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 3,
                questionPrompt = "Куда друзья решили пойти вместе?",
                options = listOf("В кафе (კაფეში)", "Домой", "На работу"),
                correctIndex = 0,
                explanation = "В тексте: 'მე მივდივარ კაფეში, წავიდეთ ერთად!'."
            ),
            StoryCheckpointItem(
                triggerAfterSentenceIndex = 5,
                questionPrompt = "Соберите фразу о напитке с молоком:",
                isBubbleAssembly = true,
                expectedWords = listOf("ცხელი", "ჩაი", "რძით"),
                wordBank = listOf("ცხელი", "ჩაი", "რძით", "წყალი", "ღვინო"),
                explanation = "В тексте: 'მე მინდა ცხელი ჩაი რძით'."
            )
        )

        return Exercise.StoryOfToday(
            storyId = "story_friends_1",
            titleGeorgian = "მეგობრები კაფეში",
            titleRussian = "Друзья в кафе",
            sentences = sentences,
            wordDictionary = dict,
            checkpoints = checkpoints
        )
    }

    /**
     * Called when user taps a word in a story to see its translation.
     * Requirement #5: "Нажатие на слово должно учитываться в прогрессе изучения слова (если нажал, значит, не очень хорошо помню)"
     */
    fun onStoryWordLookedUp(georgianWord: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val clean = georgianWord.trim().replace(Regex("[.,!?:;\"()]"), "")
            val all = allWords.value.ifEmpty { wordDao.getAllWordsOnce() }
            val word = all.find {
                it.georgian.equals(clean, ignoreCase = true) ||
                        it.getSubForms().any { f -> f.georgian.equals(clean, ignoreCase = true) }
            }
            if (word != null) {
                // User needed a hint during reading: slightly lower repetitions and schedule review soon
                val updated = word.copy(
                    repetitions = (word.repetitions - 1).coerceAtLeast(0),
                    learned = false,
                    nextReviewTimestamp = System.currentTimeMillis() + 60_000L
                )
                wordDao.updateWord(updated)
                refreshSrsDueWords()
            }
        }
    }

    /**
     * Start standard unit lesson (from Units list)
     */
    fun startLesson(categoryId: String, categoryTitle: String) {
        viewModelScope.launch {
            val words = if (categoryId == "all" || categoryId == "srs") {
                allWords.value.takeIf { it.isNotEmpty() } ?: InitialData.DEFAULT_WORDS
            } else {
                wordDao.getWordsByCategoryOnce(categoryId).ifEmpty {
                    InitialData.DEFAULT_WORDS.filter { it.category == categoryId }
                }
            }

            if (words.isEmpty()) return@launch

            val sampleWords = words.shuffled().take(5)
            val allDistractors = (allWords.value.ifEmpty { InitialData.DEFAULT_WORDS }).shuffled()
            val exercises = mutableListOf<Exercise>()

            // 1. Presentation of first word
            exercises.add(
                Exercise.NewWordPresentation(
                    word = sampleWords.first(),
                    explanation = "Ключевое слово темы '$categoryTitle'. Пример: ${sampleWords.first().exampleGeorgian}"
                )
            )

            // 2. Picture choice
            val targetForPic = sampleWords.getOrNull(1) ?: sampleWords.first()
            val picOptions = (listOf(targetForPic) + allDistractors.filter { it.id != targetForPic.id }.take(3)).shuffled()
            exercises.add(
                Exercise.PictureChoice(
                    question = "Выберите значение для: ${targetForPic.georgian}",
                    targetWord = targetForPic,
                    options = picOptions
                )
            )

            // 3. Input Russian translation (verified ONLY on "Проверить" click)
            val targetForRu = sampleWords.getOrNull(2) ?: sampleWords.first()
            exercises.add(Exercise.InputRussian(targetForRu, targetForRu.georgian))

            // 4. Input Georgian translation with native keyboard
            val targetForGe = sampleWords.getOrNull(3) ?: sampleWords.first()
            exercises.add(Exercise.InputGeorgian(targetForGe, targetForGe.russian))

            // 5. Listening
            val targetListen = sampleWords.getOrNull(0) ?: sampleWords.first()
            val listenOptions = (listOf(targetListen.russian) + allDistractors.filter { it.id != targetListen.id }.map { it.russian }.take(3)).shuffled()
            exercises.add(
                Exercise.Listening(
                    targetWord = targetListen,
                    options = listenOptions,
                    correctIndex = listenOptions.indexOf(targetListen.russian)
                )
            )

            // 6. Sentence builder
            val sentenceWord = sampleWords.firstOrNull { it.exampleGeorgian.isNotBlank() } ?: sampleWords.first()
            val geWords = sentenceWord.exampleGeorgian
                .replace(".", "").replace("!", "").replace("?", "").replace(",", "")
                .split(" ").filter { it.isNotBlank() }
            val scrambledBank = (geWords + listOf("და", "აქ", "არის").filter { !geWords.contains(it) }).shuffled()
            exercises.add(
                Exercise.BubbleSentence(
                    promptText = sentenceWord.exampleRussian.ifBlank { sentenceWord.russian },
                    promptLanguage = "RU",
                    expectedWords = geWords,
                    scrambledBank = scrambledBank,
                    fullTranslation = sentenceWord.exampleGeorgian
                )
            )

            // 7. Matching pairs
            val pairWords = (sampleWords + allDistractors).distinctBy { it.georgian }.take(4)
            exercises.add(Exercise.MatchingPairs(pairWords.map { Pair(it.georgian, it.russian) }))

            _activeLesson.value = LessonState(
                categoryId = categoryId,
                categoryTitle = categoryTitle,
                exercises = exercises,
                currentIndex = 0,
                earnedXp = 0,
                isFinished = false
            )
        }
    }

    fun onExerciseAnswer(isCorrect: Boolean, word: WordEntity? = null) {
        val current = _activeLesson.value ?: return

        var newXp = current.earnedXp
        var mistakeCount = current.mistakeCount

        if (isCorrect) {
            newXp += 10
            if (word != null) {
                val updated = SrsManager.calculateNextReview(word, SrsRating.GOOD)
                viewModelScope.launch { wordDao.updateWord(updated) }
            }
        } else {
            mistakeCount += 1
            if (word != null) {
                val updated = SrsManager.calculateNextReview(word, SrsRating.AGAIN)
                viewModelScope.launch { wordDao.updateWord(updated) }
            }
        }

        val nextIndex = current.currentIndex + 1
        val isFinished = nextIndex >= current.exercises.size

        _activeLesson.value = current.copy(
            currentIndex = nextIndex,
            earnedXp = newXp,
            mistakeCount = mistakeCount,
            isFinished = isFinished
        )

        if (isFinished) {
            completeLesson(newXp, current.categoryId)
        }
    }

    private fun completeLesson(earnedXp: Int, categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = userStats.value ?: UserStatsEntity()
            val newTotalXp = stats.totalXp + earnedXp
            val completed = stats.completedLessonsCount + 1
            val updatedStats = stats.copy(
                totalXp = newTotalXp,
                completedLessonsCount = completed,
                dailyGoalProgress = (stats.dailyGoalProgress + earnedXp).coerceAtMost(stats.dailyGoalTarget)
            ).withCompletedCategory(categoryId)
            userStatsDao.updateUserStats(updatedStats)

            // Mark completed in dailyLessons if matched
            _dailyLessons.value = _dailyLessons.value.map { daily ->
                if (daily.id == categoryId) daily.copy(isCompleted = true) else daily
            }

            refreshSrsDueWords()
        }
    }

    fun exitLesson() {
        _activeLesson.value = null
    }

    fun rateWordSrs(word: WordEntity, rating: SrsRating) {
        viewModelScope.launch {
            val updated = SrsManager.calculateNextReview(word, rating)
            wordDao.updateWord(updated)
            refreshSrsDueWords()
        }
    }

    fun addNewWordWithAi(userInput: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            try {
                val result = GeminiService.generateNewWord(userInput)
                result.onSuccess { word ->
                    wordDao.insertWord(word)
                    onComplete(true)
                }.onFailure {
                    onComplete(false)
                }
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }

    fun generateSentencesForWord(word: WordEntity) {
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            try {
                val result = GeminiService.generateSentences(word.georgian, word.russian)
                result.onSuccess { sentences ->
                    _aiGeneratedSentences.value = sentences
                }
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }

    fun explainGrammar(phrase: String) {
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            try {
                val result = GeminiService.explainGrammar(phrase)
                result.onSuccess { explanation ->
                    _grammarExplanation.value = explanation
                }
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }

    fun clearGrammarExplanation() {
        _grammarExplanation.value = null
    }

    fun createNewUnitWithAi(theme: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            try {
                val currentUnitsCount = unitDao.getCount()
                val result = GeminiService.generateNewUnit(theme, currentUnitsCount)
                result.onSuccess { generated ->
                    unitDao.insertUnit(generated.unit)
                    wordDao.insertWords(generated.words)
                    onComplete(true)
                }.onFailure {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }
}
