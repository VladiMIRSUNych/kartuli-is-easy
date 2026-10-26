package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.ButtonStyle
import com.example.ui.components.DuolingoButton
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoSubtext

@Composable
fun AiTutorScreen(
    onAskGrammar: (String) -> Unit,
    explanation: String?,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    DisposableEffect(Unit) {
        onDispose { audioHelper.release() }
    }

    var questionInput by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Как вежливо заказать хинкали и хачапури?",
        "В чём разница между 'გამარჯობა' и 'გაუმარჯოს'?",
        "Как сказать таксисту 'остановите здесь'?",
        "Как звучат буквы ყ, ჭ, წ, ღ?",
        "Как спросить на рынке 'сколько стоит' и попросить скидку?"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_tutor_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tutor Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5E5E5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DuoBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🦉", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ИИ Репетитор Kartuli",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoDarkText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DuoBlue, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "გამარჯობა! Задайте мне любой вопрос о грузинской грамматике, фразах или бытовых диалогах.",
                            fontSize = 13.sp,
                            color = DuoSubtext
                        )
                    }
                }
            }

            // Quick Suggestions
            Text(
                text = "Популярные вопросы для быта:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DuoDarkText
            )

            quickQuestions.forEach { q ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            questionInput = q
                            onAskGrammar(q)
                        }
                        .border(1.dp, Color(0xFFE5E5E8), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💬", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = q,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuoDarkText
                        )
                    }
                }
            }

            // Input Question Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5E5E5))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        placeholder = { Text("Спросите о слове, грамматике или фразе...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_tutor_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DuoBlue,
                            unfocusedBorderColor = Color(0xFFD0D7DE)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DuoBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ИИ составляет ответ...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoBlue
                            )
                        }
                    } else {
                        DuolingoButton(
                            text = "Спросить репетитора",
                            onClick = {
                                if (questionInput.isNotBlank()) {
                                    onAskGrammar(questionInput)
                                }
                            },
                            style = ButtonStyle.SECONDARY,
                            enabled = questionInput.isNotBlank(),
                            testTag = "btn_ask_ai_tutor"
                        )
                    }
                }
            }

            // AI Answer Display
            if (explanation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, DuoBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💡", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ответ ИИ-наставника:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue
                                )
                            }
                            IconButton(onClick = { audioHelper.speakGeorgian(explanation) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Озвучить", tint = DuoBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = explanation,
                            fontSize = 15.sp,
                            color = DuoDarkText,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
