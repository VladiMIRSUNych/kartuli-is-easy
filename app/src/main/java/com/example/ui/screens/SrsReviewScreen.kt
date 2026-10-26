package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioHelper
import com.example.data.WordEntity
import com.example.srs.SrsRating
import com.example.ui.components.ButtonStyle
import com.example.ui.components.DuolingoButton
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoSubtext
import com.example.ui.theme.DuoYellow

@Composable
fun SrsReviewScreen(
    dueWords: List<WordEntity>,
    allWords: List<WordEntity>,
    onRateWord: (WordEntity, SrsRating) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    DisposableEffect(Unit) {
        onDispose { audioHelper.release() }
    }

    // If no due words right now, allow review of all words or sample
    val reviewDeck = remember(dueWords, allWords) {
        if (dueWords.isNotEmpty()) dueWords else allWords.take(10)
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("srs_review_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        if (currentIndex >= reviewDeck.size) {
            // Finished SRS session
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🧠", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Интервальное повторение завершено!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoDarkText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Вы повторили ${reviewDeck.size} слов по алгоритму SM-2. Слова отложены на оптимальный срок для долговременной памяти.",
                    fontSize = 15.sp,
                    color = DuoSubtext,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                DuolingoButton(
                    text = "Вернуться на главную",
                    onClick = onBack,
                    style = ButtonStyle.PRIMARY,
                    testTag = "btn_srs_back"
                )
            }
            return@Surface
        }

        val word = reviewDeck[currentIndex]
        val progress = (currentIndex.toFloat() / reviewDeck.size).coerceIn(0f, 1f)

        LaunchedEffect(word) {
            audioHelper.speakGeorgian(word.georgian, word.transcription)
        }

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
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = DuoDarkText)
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = DuoBlue,
                    trackColor = Color(0xFFE5E5E5)
                )

                Text(
                    text = "${currentIndex + 1}/${reviewDeck.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoSubtext
                )
            }

            // Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Интервальное повторение (SM-2)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { isRevealed = !isRevealed }
                        .border(1.5.dp, Color(0xFFE0E5E8), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = word.emoji, fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = word.georgian,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoDarkText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "[ ${word.transcription} ]",
                            fontSize = 17.sp,
                            color = DuoBlue,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        IconButton(
                            onClick = { audioHelper.speakGeorgian(word.georgian, word.transcription) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DuoBlue.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Озвучить", tint = DuoBlue)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!isRevealed) {
                            Text(
                                text = "👆 Нажмите, чтобы показать ответ",
                                fontSize = 14.sp,
                                color = DuoSubtext,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF7F9FA))
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = word.russian.replaceFirstChar { it.uppercase() },
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuoGreenDark,
                                        textAlign = TextAlign.Center
                                    )

                                    if (word.exampleGeorgian.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "«${word.exampleGeorgian}»",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DuoDarkText,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = word.exampleRussian,
                                            fontSize = 13.sp,
                                            color = DuoSubtext,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SRS Rating Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isRevealed) {
                    DuolingoButton(
                        text = "Показать перевод",
                        onClick = { isRevealed = true },
                        style = ButtonStyle.SECONDARY,
                        testTag = "btn_srs_reveal"
                    )
                } else {
                    Text(
                        text = "Оцените, насколько легко вспомнили:",
                        fontSize = 13.sp,
                        color = DuoSubtext,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SrsButton(
                            label = "Снова",
                            sub = "< 1 дн",
                            color = DuoRed,
                            onClick = {
                                onRateWord(word, SrsRating.AGAIN)
                                audioHelper.playIncorrectSound()
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SrsButton(
                            label = "Трудно",
                            sub = "1 дн",
                            color = DuoOrange,
                            onClick = {
                                onRateWord(word, SrsRating.HARD)
                                audioHelper.playToneChime()
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SrsButton(
                            label = "Хорошо",
                            sub = "4 дн",
                            color = DuoGreen,
                            onClick = {
                                onRateWord(word, SrsRating.GOOD)
                                audioHelper.playCorrectSound()
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SrsButton(
                            label = "Легко",
                            sub = "7+ дн",
                            color = DuoBlue,
                            onClick = {
                                onRateWord(word, SrsRating.EASY)
                                audioHelper.playCorrectSound()
                                isRevealed = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SrsButton(
    label: String,
    sub: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() }
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = sub,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DuoDarkText
            )
        }
    }
}
