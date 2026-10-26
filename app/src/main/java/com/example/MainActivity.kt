package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KartuliViewModel
import com.example.ui.screens.AiTutorScreen
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LessonScreen
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoSubtext
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(val title: String, val icon: ImageVector) {
    LEARN("Обучение", Icons.Default.Home),
    DICTIONARY("Словарь", Icons.Default.Book),
    AI_TUTOR("ИИ Наставник", Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {

    private val viewModel: KartuliViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KartuliMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KartuliMainApp(viewModel: KartuliViewModel) {
    val activeLesson by viewModel.activeLesson.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val allWords by viewModel.allWords.collectAsStateWithLifecycle()
    val learnedWords by viewModel.learnedWords.collectAsStateWithLifecycle()
    val allUnits by viewModel.allUnits.collectAsStateWithLifecycle()
    val dailyLessons by viewModel.dailyLessons.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiGenerationLoading.collectAsStateWithLifecycle()
    val grammarExplanation by viewModel.grammarExplanation.collectAsStateWithLifecycle()
    val aiSentences by viewModel.aiGeneratedSentences.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MainTab.LEARN) }

    // If active lesson is ongoing, display LessonScreen full-screen
    if (activeLesson != null) {
        BackHandler {
            viewModel.exitLesson()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LessonScreen(
                lessonState = activeLesson!!,
                onAnswerSubmitted = { isCorrect, word ->
                    viewModel.onExerciseAnswer(isCorrect, word)
                },
                onStoryWordLookup = { georgianWord ->
                    viewModel.onStoryWordLookedUp(georgianWord)
                },
                onExitLesson = {
                    viewModel.exitLesson()
                }
            )
        }
        return
    }

    BackHandler(enabled = currentTab != MainTab.LEARN) {
        currentTab = MainTab.LEARN
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(1.dp, Color(0xFFE5E5E5))
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DuoGreenDark,
                            selectedTextColor = DuoGreenDark,
                            indicatorColor = DuoGreen.copy(alpha = 0.15f),
                            unselectedIconColor = DuoSubtext,
                            unselectedTextColor = DuoSubtext
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.LEARN -> {
                    HomeScreen(
                        userStats = userStats,
                        dailyLessons = dailyLessons,
                        allWords = allWords,
                        units = allUnits,
                        onStartDailyLesson = { dailyLesson ->
                            viewModel.startDailyLesson(dailyLesson)
                        },
                        onStartUnitLesson = { categoryId, categoryTitle ->
                            viewModel.startLesson(categoryId, categoryTitle)
                        },
                        onGenerateNewDailyLessons = {
                            viewModel.generateDailyLessons(forceRefresh = true)
                        },
                        onCreateNewUnitWithAi = { theme, callback ->
                            viewModel.createNewUnitWithAi(theme, callback)
                        },
                        isAiLoading = aiLoading
                    )
                }

                MainTab.DICTIONARY -> {
                    DictionaryScreen(
                        words = learnedWords,
                        totalAvailableWords = allWords.size
                    )
                }

                MainTab.AI_TUTOR -> {
                    AiTutorScreen(
                        onAskGrammar = { phrase ->
                            viewModel.explainGrammar(phrase)
                        },
                        explanation = grammarExplanation,
                        isLoading = aiLoading
                    )
                }
            }
        }
    }
}
