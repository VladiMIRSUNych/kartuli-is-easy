package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioHelper
import com.example.data.WordEntity
import com.example.ui.Exercise
import com.example.ui.LessonState
import com.example.ui.StoryCheckpointItem
import com.example.ui.StorySentenceItem
import com.example.ui.components.ButtonStyle
import com.example.ui.components.DuolingoButton
import com.example.ui.components.WordBubble
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedDark
import com.example.ui.theme.DuoRedLight
import com.example.ui.theme.DuoSubtext

@Composable
fun LessonScreen(
    lessonState: LessonState,
    onAnswerSubmitted: (Boolean, WordEntity?) -> Unit,
    onStoryWordLookup: (String) -> Unit = {},
    onExitLesson: () -> Unit
) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioHelper.release()
        }
    }

    if (lessonState.isFinished) {
        LessonCompleteView(
            categoryTitle = lessonState.categoryTitle,
            earnedXp = lessonState.earnedXp,
            mistakeCount = lessonState.mistakeCount,
            onContinue = onExitLesson
        )
        return
    }

    val currentExercise = lessonState.exercises.getOrNull(lessonState.currentIndex) ?: return
    val progress = (lessonState.currentIndex.toFloat() / lessonState.exercises.size).coerceIn(0f, 1f)

    var hasCheckedAnswer by remember(lessonState.currentIndex) { mutableStateOf(false) }
    var isAnswerCorrect by remember(lessonState.currentIndex) { mutableStateOf(false) }
    var feedbackMessage by remember(lessonState.currentIndex) { mutableStateOf("") }
    var associatedWord by remember(lessonState.currentIndex) { mutableStateOf<WordEntity?>(null) }

    // Draft answer state held at screen level to allow "Проверить" click check
    var draftAnswerText by remember(lessonState.currentIndex) { mutableStateOf("") }
    var draftOptionIndex by remember(lessonState.currentIndex) { mutableStateOf<Int?>(null) }
    val draftBubbleSelectedWords = remember(lessonState.currentIndex) { mutableStateListOf<String>() }

    // Story Lesson has internal progression
    if (currentExercise is Exercise.StoryOfToday) {
        StoryOfTodayExerciseView(
            exercise = currentExercise,
            audioHelper = audioHelper,
            onWordLookup = onStoryWordLookup,
            onStoryCompleted = {
                onAnswerSubmitted(true, null)
            },
            onExit = onExitLesson
        )
        return
    }

    // New Word Presentation has internal confirmation
    if (currentExercise is Exercise.NewWordPresentation) {
        NewWordPresentationView(
            word = currentExercise.word,
            explanation = currentExercise.explanation,
            audioHelper = audioHelper,
            onDone = {
                onAnswerSubmitted(true, currentExercise.word)
            },
            onExit = onExitLesson
        )
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Exit + Clean Progress Bar (no hearts, no energy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExitLesson,
                    modifier = Modifier.testTag("btn_exit_lesson")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Выйти из урока",
                        tint = Color(0xFF888888)
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = DuoGreen,
                    trackColor = Color(0xFFE5E5E5)
                )

                Text(
                    text = "${lessonState.currentIndex + 1}/${lessonState.exercises.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoSubtext
                )
            }

            // Exercise Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                when (currentExercise) {
                    is Exercise.PictureChoice -> {
                        associatedWord = currentExercise.targetWord
                        PictureChoiceExerciseView(
                            exercise = currentExercise,
                            audioHelper = audioHelper,
                            selectedIndex = draftOptionIndex,
                            onSelectIndex = { draftOptionIndex = it }
                        )
                    }

                    is Exercise.InputRussian -> {
                        associatedWord = currentExercise.targetWord
                        InputRussianExerciseView(
                            word = currentExercise.targetWord,
                            audioHelper = audioHelper,
                            currentText = draftAnswerText,
                            onTextChange = { draftAnswerText = it }
                        )
                    }

                    is Exercise.InputGeorgian -> {
                        associatedWord = currentExercise.targetWord
                        InputGeorgianExerciseView(
                            word = currentExercise.targetWord,
                            promptRussian = currentExercise.promptRussian,
                            audioHelper = audioHelper,
                            currentText = draftAnswerText,
                            onTextChange = { draftAnswerText = it }
                        )
                    }

                    is Exercise.Listening -> {
                        associatedWord = currentExercise.targetWord
                        ListeningExerciseView(
                            exercise = currentExercise,
                            audioHelper = audioHelper,
                            selectedIndex = draftOptionIndex,
                            onSelectIndex = { draftOptionIndex = it }
                        )
                    }

                    is Exercise.BubbleSentence -> {
                        BubbleSentenceExerciseView(
                            exercise = currentExercise,
                            selectedWords = draftBubbleSelectedWords
                        )
                    }

                    is Exercise.MatchingPairs -> {
                        MatchingPairsExerciseView(
                            exercise = currentExercise,
                            audioHelper = audioHelper,
                            onAllMatched = {
                                hasCheckedAnswer = true
                                isAnswerCorrect = true
                                feedbackMessage = "Все пары сопоставлены правильно!"
                            }
                        )
                    }

                    is Exercise.Pronunciation -> {
                        PronunciationExerciseView(
                            exercise = currentExercise,
                            audioHelper = audioHelper,
                            onComplete = {
                                hasCheckedAnswer = true
                                isAnswerCorrect = true
                                feedbackMessage = "Отличное произношение!"
                            }
                        )
                    }

                    else -> Unit
                }
            }

            // Bottom Feedback Banner & Action Button
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(visible = hasCheckedAnswer) {
                    val bgColor = if (isAnswerCorrect) DuoGreenLight else DuoRedLight
                    val textColor = if (isAnswerCorrect) DuoGreenDark else DuoRedDark

                    LaunchedEffect(hasCheckedAnswer) {
                        if (isAnswerCorrect) {
                            audioHelper.playCorrectSound()
                        } else {
                            audioHelper.playIncorrectSound()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAnswerCorrect) "✓" else "💡",
                                fontSize = 26.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = if (isAnswerCorrect) "ВЕРНО!" else "НЕ СОВСЕМ...",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = textColor
                                )
                                Text(
                                    text = feedbackMessage,
                                    fontSize = 14.sp,
                                    color = DuoDarkText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Determine if "Проверить" is enabled
                val isCheckEnabled = when (currentExercise) {
                    is Exercise.InputRussian -> draftAnswerText.isNotBlank()
                    is Exercise.InputGeorgian -> draftAnswerText.isNotBlank()
                    is Exercise.PictureChoice -> draftOptionIndex != null
                    is Exercise.Listening -> draftOptionIndex != null
                    is Exercise.BubbleSentence -> draftBubbleSelectedWords.isNotEmpty()
                    is Exercise.MatchingPairs -> hasCheckedAnswer
                    is Exercise.Pronunciation -> true
                    else -> true
                }

                DuolingoButton(
                    text = if (hasCheckedAnswer) "Продолжить" else "Проверить",
                    onClick = {
                        if (!hasCheckedAnswer) {
                            // EXECUTE VERIFICATION ONLY UPON CLICKING "Проверить" (Requirement #9)
                            when (currentExercise) {
                                is Exercise.InputRussian -> {
                                    val correct = com.example.util.TranslationValidator.isRussianTranslationCorrect(
                                        userInput = draftAnswerText,
                                        primaryRussian = currentExercise.targetWord.russian,
                                        synonyms = currentExercise.targetWord.getSynonymList()
                                    )
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = correct
                                    feedbackMessage = if (correct) "Превосходно!" else "Правильный ответ: ${currentExercise.targetWord.russian}"
                                }

                                is Exercise.InputGeorgian -> {
                                    val correct = com.example.util.TranslationValidator.isGeorgianTranslationCorrect(
                                        userInput = draftAnswerText,
                                        targetGeorgian = currentExercise.targetWord.georgian,
                                        transcription = currentExercise.targetWord.transcription,
                                        phoneticCyrillic = audioHelper.georgianToCyrillicPhonetics(currentExercise.targetWord.georgian)
                                    )
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = correct
                                    feedbackMessage = if (correct) "Блестяще!" else "Правильный ответ: ${currentExercise.targetWord.georgian}"
                                }

                                is Exercise.PictureChoice -> {
                                    val selected = currentExercise.options.getOrNull(draftOptionIndex ?: -1)
                                    val correct = selected?.id == currentExercise.targetWord.id
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = correct
                                    feedbackMessage = if (correct) "Правильно!" else "Правильный ответ: ${currentExercise.targetWord.russian}"
                                }

                                is Exercise.Listening -> {
                                    val correct = draftOptionIndex == currentExercise.correctIndex
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = correct
                                    feedbackMessage = if (correct) "Отличный слух!" else "Правильный ответ: ${currentExercise.options[currentExercise.correctIndex]}"
                                }

                                is Exercise.BubbleSentence -> {
                                    val userSentence = draftBubbleSelectedWords.joinToString(" ").trim()
                                    val expectedSentence = currentExercise.expectedWords.joinToString(" ").trim()
                                    val correct = userSentence == expectedSentence
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = correct
                                    feedbackMessage = if (correct) "Отлично составлено!" else "Правильно: $expectedSentence"
                                }

                                else -> {
                                    hasCheckedAnswer = true
                                    isAnswerCorrect = true
                                }
                            }
                        } else {
                            // Move to next exercise
                            onAnswerSubmitted(isAnswerCorrect, associatedWord)
                        }
                    },
                    style = if (hasCheckedAnswer && !isAnswerCorrect) ButtonStyle.DANGER else ButtonStyle.PRIMARY,
                    enabled = if (hasCheckedAnswer) true else isCheckEnabled,
                    testTag = "btn_check_answer"
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. NEW WORD PRESENTATION (Requirement #3)
// -------------------------------------------------------------------------------------------------
@Composable
fun NewWordPresentationView(
    word: WordEntity,
    explanation: String,
    audioHelper: AudioHelper,
    onDone: () -> Unit,
    onExit: () -> Unit
) {
    var writingDraft by remember { mutableStateOf("") }
    var hasInteractedWithAudio by remember { mutableStateOf(false) }

    LaunchedEffect(word) {
        audioHelper.speakGeorgian(word.georgian, word.transcription)
        hasInteractedWithAudio = true
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("new_word_presentation"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "Выйти", tint = DuoSubtext)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DuoBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "НОВОЕ СЛОВО",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoBlue
                    )
                }
            }

            // Word Presentation Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = word.emoji,
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Georgian Word
                Text(
                    text = word.georgian,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoDarkText,
                    textAlign = TextAlign.Center
                )

                // Phonetics
                Text(
                    text = "[ ${audioHelper.georgianToCyrillicPhonetics(word.georgian)} ]",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DuoBlue,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Audio Button
                IconButton(
                    onClick = {
                        audioHelper.speakGeorgian(word.georgian, word.transcription)
                        hasInteractedWithAudio = true
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(DuoBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Слушать произношение",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Russian Translation
                Text(
                    text = word.russian.replaceFirstChar { it.uppercase() },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Explanation & Usage Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Как употребляется:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = explanation,
                            fontSize = 14.sp,
                            color = DuoSubtext,
                            lineHeight = 20.sp
                        )

                        if (word.exampleGeorgian.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0F4F8))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = word.exampleGeorgian,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DuoDarkText
                                    )
                                    Text(
                                        text = word.exampleRussian,
                                        fontSize = 13.sp,
                                        color = DuoSubtext
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 2: Practice typing to lock into memory (Requirement #3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFD)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6E4F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✍️ Напишите слово для закрепления:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoDarkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = writingDraft,
                            onValueChange = { writingDraft = it },
                            placeholder = { Text("Введите: ${word.georgian}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_word_practice_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DuoBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Continue Button
            DuolingoButton(
                text = "Запомнил, дальше",
                onClick = onDone,
                style = ButtonStyle.PRIMARY,
                testTag = "btn_new_word_continue"
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. INPUT RUSSIAN TRANSLATION (Requirement #9: Checked ONLY on "Проверить" click!)
// -------------------------------------------------------------------------------------------------
@Composable
fun InputRussianExerciseView(
    word: WordEntity,
    audioHelper: AudioHelper,
    currentText: String,
    onTextChange: (String) -> Unit
) {
    LaunchedEffect(word) {
        audioHelper.speakGeorgian(word.georgian, word.transcription)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Напишите перевод на русский:",
            fontSize = 16.sp,
            color = DuoSubtext,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { audioHelper.speakGeorgian(word.georgian, word.transcription) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DuoBlue.copy(alpha = 0.15f))
                    .size(44.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Слушать", tint = DuoBlue)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = word.georgian,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = DuoDarkText
            )
        }

        Text(
            text = "[ ${audioHelper.georgianToCyrillicPhonetics(word.georgian)} ]",
            fontSize = 16.sp,
            color = DuoBlue,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = currentText,
            onValueChange = onTextChange,
            placeholder = { Text("Введите перевод на русский...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_russian_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DuoBlue,
                unfocusedBorderColor = Color(0xFFD0D7DE)
            )
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 3. INPUT GEORGIAN TRANSLATION (Requirement #7: Native system keyboard!)
// -------------------------------------------------------------------------------------------------
@Composable
fun InputGeorgianExerciseView(
    word: WordEntity,
    promptRussian: String,
    audioHelper: AudioHelper,
    currentText: String,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Напишите по-грузински (родной клавиатурой):",
            fontSize = 16.sp,
            color = DuoSubtext,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = promptRussian.replaceFirstChar { it.uppercase() },
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = DuoDarkText,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Произношение: [ ${audioHelper.georgianToCyrillicPhonetics(word.georgian)} ]",
            fontSize = 15.sp,
            color = DuoBlue,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = currentText,
            onValueChange = onTextChange,
            placeholder = { Text("Введите на грузинском...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_georgian_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DuoBlue,
                unfocusedBorderColor = Color(0xFFD0D7DE)
            )
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 4. SENTENCE BUBBLES BUILDER (Requirement #8: Phrase assembly)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BubbleSentenceExerciseView(
    exercise: Exercise.BubbleSentence,
    selectedWords: MutableList<String>
) {
    val unselectedWords = remember(exercise, selectedWords.size) {
        val remaining = exercise.scrambledBank.toMutableList()
        selectedWords.forEach { remaining.remove(it) }
        remaining
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Соберите предложение:",
            fontSize = 16.sp,
            color = DuoSubtext,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Russian prompt
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
        ) {
            Text(
                text = exercise.promptText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DuoDarkText,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Answer slots / selected bubbles
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(2.dp, DuoBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (selectedWords.isEmpty()) {
                Text(
                    text = "Нажимайте на слова снизу...",
                    color = Color(0xFFAAAAAA),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedWords.forEachIndexed { idx, word ->
                        WordBubble(
                            word = word,
                            onClick = { selectedWords.removeAt(idx) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bank of word bubbles
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            unselectedWords.forEach { word ->
                WordBubble(
                    word = word,
                    onClick = { selectedWords.add(word) }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. PICTURE CHOICE
// -------------------------------------------------------------------------------------------------
@Composable
fun PictureChoiceExerciseView(
    exercise: Exercise.PictureChoice,
    audioHelper: AudioHelper,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    LaunchedEffect(exercise) {
        audioHelper.speakGeorgian(exercise.targetWord.georgian, exercise.targetWord.transcription)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = exercise.question,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuoDarkText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        exercise.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectIndex(index) }
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) DuoBlue else Color(0xFFE5E5E5),
                        shape = RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DuoBlue.copy(alpha = 0.08f) else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = option.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = option.russian.replaceFirstChar { it.uppercase() },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoDarkText
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. LISTENING EXERCISE
// -------------------------------------------------------------------------------------------------
@Composable
fun ListeningExerciseView(
    exercise: Exercise.Listening,
    audioHelper: AudioHelper,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    LaunchedEffect(exercise) {
        audioHelper.speakGeorgian(exercise.targetWord.georgian, exercise.targetWord.transcription)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Послушайте и выберите правильный перевод:",
            fontSize = 16.sp,
            color = DuoSubtext,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big speaker button
        IconButton(
            onClick = { audioHelper.speakGeorgian(exercise.targetWord.georgian, exercise.targetWord.transcription) },
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DuoBlue)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Повторить аудио",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        exercise.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectIndex(index) }
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) DuoBlue else Color(0xFFE5E5E5),
                        shape = RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DuoBlue.copy(alpha = 0.08f) else Color.White
                )
            ) {
                Text(
                    text = option.replaceFirstChar { it.uppercase() },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoDarkText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 7. MATCHING PAIRS
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchingPairsExerciseView(
    exercise: Exercise.MatchingPairs,
    audioHelper: AudioHelper,
    onAllMatched: () -> Unit
) {
    val georgianTiles = remember(exercise) { exercise.pairs.map { it.first }.shuffled() }
    val russianTiles = remember(exercise) { exercise.pairs.map { it.second }.shuffled() }

    var selectedGeorgian by remember { mutableStateOf<String?>(null) }
    var selectedRussian by remember { mutableStateOf<String?>(null) }
    val matchedGeorgian = remember { mutableStateListOf<String>() }
    val matchedRussian = remember { mutableStateListOf<String>() }

    fun checkMatch() {
        if (selectedGeorgian != null && selectedRussian != null) {
            val isMatch = exercise.pairs.any { it.first == selectedGeorgian && it.second == selectedRussian }
            if (isMatch) {
                matchedGeorgian.add(selectedGeorgian!!)
                matchedRussian.add(selectedRussian!!)
                audioHelper.playCorrectSound()
            } else {
                audioHelper.playIncorrectSound()
            }
            selectedGeorgian = null
            selectedRussian = null

            if (matchedGeorgian.size == exercise.pairs.size) {
                onAllMatched()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Соедините подходящие пары:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DuoDarkText
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Georgian Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                georgianTiles.forEach { ge ->
                    val isMatched = matchedGeorgian.contains(ge)
                    val isSelected = selectedGeorgian == ge
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched) {
                                selectedGeorgian = ge
                                audioHelper.speakGeorgian(ge)
                                checkMatch()
                            }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = when {
                                    isMatched -> Color(0xFFC8E6C9)
                                    isSelected -> DuoBlue
                                    else -> Color(0xFFE5E5E5)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMatched -> Color(0xFFE8F5E9)
                                isSelected -> DuoBlue.copy(alpha = 0.12f)
                                else -> Color.White
                            }
                        )
                    ) {
                        Text(
                            text = ge,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) Color(0xFF81C784) else DuoDarkText,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Russian Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                russianTiles.forEach { ru ->
                    val isMatched = matchedRussian.contains(ru)
                    val isSelected = selectedRussian == ru
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched) {
                                selectedRussian = ru
                                checkMatch()
                            }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = when {
                                    isMatched -> Color(0xFFC8E6C9)
                                    isSelected -> DuoBlue
                                    else -> Color(0xFFE5E5E5)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMatched -> Color(0xFFE8F5E9)
                                isSelected -> DuoBlue.copy(alpha = 0.12f)
                                else -> Color.White
                            }
                        )
                    ) {
                        Text(
                            text = ru,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMatched) Color(0xFF81C784) else DuoDarkText,
                            modifier = Modifier.padding(14.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 8. INTERACTIVE STORY OF TODAY (Requirement #5)
// Sentence-by-sentence reading with audio, tap-to-translate with SRS logging,
// and checkpoints every 2-3 sentences.
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryOfTodayExerciseView(
    exercise: Exercise.StoryOfToday,
    audioHelper: AudioHelper,
    onWordLookup: (String) -> Unit,
    onStoryCompleted: () -> Unit,
    onExit: () -> Unit
) {
    // Current visible sentence index (0 to size-1)
    var currentSentenceIndex by remember { mutableStateOf(0) }
    var activeCheckpoint by remember { mutableStateOf<StoryCheckpointItem?>(null) }
    var checkpointAnsweredCorrectly by remember { mutableStateOf(false) }
    var checkpointOptionSelected by remember { mutableStateOf<Int?>(null) }
    val checkpointBubbleWords = remember { mutableStateListOf<String>() }

    // Clicked word translation tooltip
    var selectedWordTranslation by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Play audio when advancing to a new sentence
    LaunchedEffect(currentSentenceIndex) {
        val sentence = exercise.sentences.getOrNull(currentSentenceIndex)
        if (sentence != null) {
            audioHelper.speakGeorgian(sentence.georgian)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("story_lesson_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "Выйти", tint = DuoSubtext)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.titleGeorgian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoDarkText
                    )
                    Text(
                        text = exercise.titleRussian,
                        fontSize = 12.sp,
                        color = DuoSubtext
                    )
                }
            }

            // Story Body: Preserves all previously shown sentences!
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Instruction tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEBF3FB))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Нажмите на любое слово в тексте, чтобы увидеть его перевод",
                            fontSize = 12.sp,
                            color = DuoBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentences rendered sequentially up to currentSentenceIndex
                    val visibleSentences = exercise.sentences.take(currentSentenceIndex + 1)
                    visibleSentences.forEachIndexed { idx, sentence ->
                        val isCurrent = idx == currentSentenceIndex

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) DuoBlue else Color(0xFFE5E5E5),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color.White else Color(0xFFFCFDFD)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { audioHelper.speakGeorgian(sentence.georgian) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DuoBlue.copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Озвучить предложение",
                                            tint = DuoBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Interactive words flow! Every word is clickable
                                    FlowRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val rawWords = sentence.georgian.split(" ")
                                        rawWords.forEach { rawWord ->
                                            val cleanWord = rawWord.replace(Regex("[.,!?:;\"()]"), "").trim()
                                            val translation = exercise.wordDictionary[cleanWord]
                                                ?: exercise.wordDictionary[cleanWord.lowercase()]

                                            Text(
                                                text = rawWord,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DuoDarkText,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        if (translation != null) {
                                                            selectedWordTranslation = Pair(cleanWord, translation)
                                                            onWordLookup(cleanWord) // Logs to SRS!
                                                        }
                                                    }
                                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = sentence.russian,
                                    fontSize = 13.sp,
                                    color = DuoSubtext,
                                    modifier = Modifier.padding(top = 8.dp, start = 44.dp)
                                )
                            }
                        }
                    }

                    // Word Translation Tooltip Banner (if user tapped a word)
                    if (selectedWordTranslation != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF8E1))
                                .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedWordTranslation?.first} — ${selectedWordTranslation?.second}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuoDarkText
                                    )
                                    Text(
                                        text = "Добавлено на повторение в SRS",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF57F17)
                                    )
                                }
                                IconButton(
                                    onClick = { selectedWordTranslation = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = DuoSubtext)
                                }
                            }
                        }
                    }

                    // Checkpoint Challenge Banner (if one is triggered)
                    if (activeCheckpoint != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, DuoBlue, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "❓ Задание по прочитанному:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activeCheckpoint!!.questionPrompt,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoDarkText
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                if (activeCheckpoint!!.isBubbleAssembly) {
                                    // Bubble assembly challenge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF7F9FA))
                                            .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(10.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            checkpointBubbleWords.forEachIndexed { i, w ->
                                                WordBubble(word = w, onClick = { checkpointBubbleWords.removeAt(i) })
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        activeCheckpoint!!.wordBank.filter { it !in checkpointBubbleWords }.forEach { w ->
                                            WordBubble(word = w, onClick = { checkpointBubbleWords.add(w) })
                                        }
                                    }
                                } else {
                                    // Multiple choice options
                                    activeCheckpoint!!.options.forEachIndexed { optIdx, option ->
                                        val isSel = checkpointOptionSelected == optIdx
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable { checkpointOptionSelected = optIdx }
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) DuoBlue else Color(0xFFE5E5E5),
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) DuoBlue.copy(alpha = 0.08f) else Color(0xFFF7F9FA)
                                            )
                                        ) {
                                            Text(
                                                text = option,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DuoDarkText,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Bottom Navigation & Actions
            Column(modifier = Modifier.fillMaxWidth()) {
                val isAtCheckpoint = activeCheckpoint != null

                if (isAtCheckpoint) {
                    DuolingoButton(
                        text = "Проверить ответ",
                        onClick = {
                            val checkpoint = activeCheckpoint!!
                            val isCorrect = if (checkpoint.isBubbleAssembly) {
                                checkpointBubbleWords.joinToString(" ") == checkpoint.expectedWords.joinToString(" ")
                            } else {
                                checkpointOptionSelected == checkpoint.correctIndex
                            }

                            if (isCorrect) {
                                audioHelper.playCorrectSound()
                                activeCheckpoint = null
                                checkpointOptionSelected = null
                                checkpointBubbleWords.clear()
                                // Advance sentence
                                if (currentSentenceIndex < exercise.sentences.size - 1) {
                                    currentSentenceIndex++
                                } else {
                                    onStoryCompleted()
                                }
                            } else {
                                audioHelper.playIncorrectSound()
                            }
                        },
                        style = ButtonStyle.PRIMARY,
                        enabled = if (activeCheckpoint!!.isBubbleAssembly) checkpointBubbleWords.isNotEmpty() else checkpointOptionSelected != null
                    )
                } else {
                    val isLastSentence = currentSentenceIndex >= exercise.sentences.size - 1
                    DuolingoButton(
                        text = if (isLastSentence) "Завершить историю" else "Следующее предложение",
                        onClick = {
                            if (isLastSentence) {
                                onStoryCompleted()
                            } else {
                                // Check if a checkpoint should trigger after this sentence index
                                val nextCheckpoint = exercise.checkpoints.find { it.triggerAfterSentenceIndex == currentSentenceIndex }
                                if (nextCheckpoint != null) {
                                    activeCheckpoint = nextCheckpoint
                                } else {
                                    currentSentenceIndex++
                                }
                            }
                        },
                        style = ButtonStyle.PRIMARY,
                        testTag = "btn_next_story_sentence"
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 9. PRONUNCIATION / SPEECH EXERCISE
// -------------------------------------------------------------------------------------------------
@Composable
fun PronunciationExerciseView(
    exercise: Exercise.Pronunciation,
    audioHelper: AudioHelper,
    onComplete: () -> Unit
) {
    LaunchedEffect(exercise) {
        audioHelper.speakGeorgian(exercise.targetPhrase, exercise.transcription)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Прослушайте и повторите вслух:",
            fontSize = 16.sp,
            color = DuoSubtext,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = exercise.targetPhrase,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DuoDarkText,
            textAlign = TextAlign.Center
        )

        Text(
            text = "[ ${audioHelper.georgianToCyrillicPhonetics(exercise.targetPhrase)} ]",
            fontSize = 16.sp,
            color = DuoBlue,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Text(
            text = exercise.translation,
            fontSize = 18.sp,
            color = DuoGreenDark,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(28.dp))

        IconButton(
            onClick = { audioHelper.speakGeorgian(exercise.targetPhrase, exercise.transcription) },
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DuoBlue)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Слушать",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DuolingoButton(
            text = "Повторил вслух",
            onClick = onComplete,
            style = ButtonStyle.PRIMARY
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 10. LESSON COMPLETE VIEW (No hearts, dignified completion)
// -------------------------------------------------------------------------------------------------
@Composable
fun LessonCompleteView(
    categoryTitle: String,
    earnedXp: Int,
    mistakeCount: Int,
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_complete_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Урок пройден!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoDarkText
                )
                Text(
                    text = categoryTitle,
                    fontSize = 16.sp,
                    color = DuoSubtext,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Опыт", fontSize = 12.sp, color = DuoSubtext)
                            Text(
                                text = "+$earnedXp XP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoGreenDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Точность", fontSize = 12.sp, color = DuoSubtext)
                            Text(
                                text = if (mistakeCount == 0) "100%" else "${(85 - mistakeCount * 5).coerceAtLeast(60)}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue
                            )
                        }
                    }
                }
            }

            DuolingoButton(
                text = "Отлично, на главную",
                onClick = onContinue,
                style = ButtonStyle.PRIMARY,
                testTag = "btn_lesson_complete_continue"
            )
        }
    }
}
