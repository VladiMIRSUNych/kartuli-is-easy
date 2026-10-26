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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.data.UnitEntity
import com.example.data.UserStatsEntity
import com.example.data.WordEntity
import com.example.ui.DailyLessonInfo
import com.example.ui.components.ButtonStyle
import com.example.ui.components.DuolingoButton
import com.example.ui.components.HeaderStatsBar
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoSubtext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userStats: UserStatsEntity?,
    dailyLessons: List<DailyLessonInfo>,
    allWords: List<WordEntity>,
    units: List<UnitEntity>,
    onStartDailyLesson: (DailyLessonInfo) -> Unit,
    onStartUnitLesson: (String, String) -> Unit,
    onGenerateNewDailyLessons: () -> Unit,
    onCreateNewUnitWithAi: (String, (Boolean) -> Unit) -> Unit,
    isAiLoading: Boolean
) {
    val displayUnits = if (units.isNotEmpty()) units else InitialData.DEFAULT_UNITS
    val completedCount = dailyLessons.count { it.isCompleted }

    var showCreateUnitSheet by remember { mutableStateOf(false) }
    var customUnitThemeInput by remember { mutableStateOf("") }

    val presetUnitThemes = listOf(
        Pair("💊 Аптека и лекарства", "აფთიაქი"),
        Pair("🏦 Банк и обмен валют", "ბანკი"),
        Pair("🏔️ Поездка в горы", "მთები"),
        Pair("🔑 Аренда квартиры", "ბინა"),
        Pair("🚗 Авто и такси", "მანქანა"),
        Pair("🛒 Супермаркет и базар", "მაღაზია"),
        Pair("☕ Кофейни и десерты", "ყავა"),
        Pair("🍷 Грузинские тосты и вино", "ღვინო")
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header stats (dignified, no hearts or flames)
            HeaderStatsBar(
                stats = userStats,
                totalWordsCount = allWords.size,
                completedTodayCount = completedCount
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                // Section 1: Daily Program Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🎯 План обучения на день",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DuoDarkText
                                    )
                                    Text(
                                        text = "3 урока • примерно 30 минут обучения",
                                        fontSize = 13.sp,
                                        color = DuoSubtext,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (completedCount == 3) Color(0xFFE8F5E9) else Color(0xFFF0F4F8))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$completedCount из 3 пройдено",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (completedCount == 3) DuoGreenDark else DuoBlue
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { (completedCount / 3f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = DuoGreen,
                                trackColor = Color(0xFFEEEEEE)
                            )
                        }
                    }
                }

                // Section 2: The 3 Daily Lessons (Requirement #11, #12)
                item {
                    Text(
                        text = "Уроки на сегодня:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoDarkText,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                items(dailyLessons, key = { it.id }) { lesson ->
                    DailyLessonCard(
                        lesson = lesson,
                        onStart = { onStartDailyLesson(lesson) }
                    )
                }

                // Button: "Сгенерировать новые уроки" (Requirement #11)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGenerateNewDailyLessons() }
                            .border(1.dp, Color(0xFFD6E4F0), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = DuoBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Сгенерировать новые уроки",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue
                                )
                                Text(
                                    text = "Хотите позаниматься ещё 30 минут? Сформировать свежий набор",
                                    fontSize = 11.sp,
                                    color = DuoSubtext
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Section 3: Topic Explorer
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Темы и модули курса:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoDarkText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .clickable { showCreateUnitSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ Тема с ИИ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoGreenDark
                            )
                        }
                    }
                }

                items(displayUnits, key = { it.id }) { unit ->
                    UnitTopicCard(
                        unit = unit,
                        onClick = { onStartUnitLesson(unit.id, unit.title) }
                    )
                }
            }
        }

        // Add Unit Sheet
        if (showCreateUnitSheet) {
            ModalBottomSheet(
                onDismissRequest = { if (!isAiLoading) showCreateUnitSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "✨ Создать тему с помощью Gemini AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoDarkText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ИИ сгенерирует практический модуль со словами и диалогами",
                        fontSize = 13.sp,
                        color = DuoSubtext
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetUnitThemes.forEach { (title, _) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0F4F8))
                                    .clickable { customUnitThemeInput = title.substringAfter(" ") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = title, fontSize = 12.sp, color = DuoDarkText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customUnitThemeInput,
                        onValueChange = { customUnitThemeInput = it },
                        placeholder = { Text("Например: В ресторане, Больница, Вождение...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        DuolingoButton(
                            text = "Сгенерировать модуль",
                            onClick = {
                                if (customUnitThemeInput.isNotBlank()) {
                                    onCreateNewUnitWithAi(customUnitThemeInput) { success ->
                                        if (success) {
                                            customUnitThemeInput = ""
                                            showCreateUnitSheet = false
                                        }
                                    }
                                }
                            },
                            style = ButtonStyle.PRIMARY,
                            enabled = customUnitThemeInput.isNotBlank()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DailyLessonCard(
    lesson: DailyLessonInfo,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStart() }
            .border(
                width = 1.dp,
                color = if (lesson.isCompleted) Color(0xFFC8E6C9) else Color(0xFFE5E5E5),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (lesson.isCompleted) Color(0xFFF9FDF9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = lesson.iconEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = lesson.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoDarkText
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "${lesson.durationText} • ${lesson.exerciseCountText}",
                                fontSize = 12.sp,
                                color = DuoSubtext
                            )
                        }
                    }
                }

                // Completion status indicator
                if (lesson.isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Пройден",
                            tint = DuoGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Пройден",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = lesson.subtitle,
                fontSize = 13.sp,
                color = DuoSubtext,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            DuolingoButton(
                text = if (lesson.isCompleted) "Пройти снова" else "Начать урок",
                onClick = onStart,
                style = if (lesson.isCompleted) ButtonStyle.OUTLINE else ButtonStyle.PRIMARY,
                modifier = Modifier.height(44.dp)
            )
        }
    }
}

@Composable
fun UnitTopicCard(
    unit: UnitEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = unit.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoDarkText
                )
                Text(
                    text = unit.description,
                    fontSize = 12.sp,
                    color = DuoSubtext,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Учить",
                tint = DuoBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
