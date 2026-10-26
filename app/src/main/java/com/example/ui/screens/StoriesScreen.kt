package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioHelper
import com.example.data.StoryEntity
import com.example.ui.components.ButtonStyle
import com.example.ui.components.DuolingoButton
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoSubtext

@Composable
fun StoriesScreen(
    stories: List<StoryEntity>,
    onStartStoryPractice: (StoryEntity) -> Unit
) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    DisposableEffect(Unit) {
        onDispose { audioHelper.release() }
    }

    var selectedStory by remember { mutableStateOf<StoryEntity?>(null) }
    var showTranslation by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var answerSubmitted by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stories_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        if (selectedStory == null) {
            // Story List
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Истории и чтение",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoDarkText
                    )
                    Text(
                        text = "Реалистичные бытовые ситуации в Грузии с проверкой понимания",
                        fontSize = 13.sp,
                        color = DuoSubtext
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
                ) {
                    items(stories, key = { it.id }) { story ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStory = story
                                    showTranslation = false
                                    selectedOptionIndex = null
                                    answerSubmitted = false
                                }
                                .border(1.5.dp, Color(0xFFE5E5E5), RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(DuoBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (story.category) {
                                            "Ресторан" -> "🥟"
                                            "Город" -> "🚕"
                                            "Рынок" -> "🧀"
                                            else -> "📖"
                                        },
                                        fontSize = 26.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DuoBlue.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = story.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DuoBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = story.titleGeorgian,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuoDarkText
                                    )
                                    Text(
                                        text = story.titleRussian,
                                        fontSize = 13.sp,
                                        color = DuoSubtext
                                    )
                                }

                                Text(
                                    text = "Читать ›",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoGreenDark
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Active Story Reading & Comprehension View
            val story = selectedStory!!
            val options = listOf(story.option1, story.option2, story.option3, story.option4)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹ Назад к историям",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoBlue,
                        modifier = Modifier
                            .clickable { selectedStory = null }
                            .padding(8.dp)
                    )

                    IconButton(
                        onClick = { audioHelper.speakGeorgian(story.contentGeorgian) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DuoBlue.copy(alpha = 0.12f))
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Озвучить всю историю", tint = DuoBlue)
                    }
                }

                // Story Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = story.titleGeorgian,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoDarkText
                        )
                        Text(
                            text = story.titleRussian,
                            fontSize = 14.sp,
                            color = DuoSubtext,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = story.contentGeorgian,
                            fontSize = 16.sp,
                            color = DuoDarkText,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showTranslation = !showTranslation }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = if (showTranslation) "👁️ Скрыть русский перевод" else "👁️ Показать русский перевод",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoBlue
                            )
                        }

                        if (showTranslation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF3F6F8))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = story.contentRussian,
                                    fontSize = 13.sp,
                                    color = DuoSubtext,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question
                        Text(
                            text = story.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoDarkText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        options.forEachIndexed { index, option ->
                            val isSelected = selectedOptionIndex == index
                            val isCorrectAnswer = index == story.correctIndex

                            val borderColor = when {
                                answerSubmitted && isCorrectAnswer -> DuoGreen
                                answerSubmitted && isSelected && !isCorrectAnswer -> DuoRed
                                isSelected -> DuoBlue
                                else -> Color(0xFFE5E5E5)
                            }

                            val bgColor = when {
                                answerSubmitted && isCorrectAnswer -> DuoGreen.copy(alpha = 0.12f)
                                answerSubmitted && isSelected && !isCorrectAnswer -> DuoRed.copy(alpha = 0.12f)
                                isSelected -> DuoBlue.copy(alpha = 0.1f)
                                else -> Color.White
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !answerSubmitted) {
                                        selectedOptionIndex = index
                                    }
                                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor)
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

                // Action
                if (!answerSubmitted) {
                    DuolingoButton(
                        text = "Проверить ответ",
                        onClick = {
                            if (selectedOptionIndex != null) {
                                answerSubmitted = true
                                if (selectedOptionIndex == story.correctIndex) {
                                    audioHelper.playCorrectSound()
                                } else {
                                    audioHelper.playIncorrectSound()
                                }
                            }
                        },
                        style = ButtonStyle.PRIMARY,
                        enabled = selectedOptionIndex != null
                    )
                } else {
                    DuolingoButton(
                        text = "Отлично! Завершить чтение",
                        onClick = { selectedStory = null },
                        style = ButtonStyle.PRIMARY
                    )
                }
            }
        }
    }
}
