package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioHelper
import com.example.data.WordEntity
import com.example.data.WordSubForm
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoSubtext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    words: List<WordEntity>,
    totalAvailableWords: Int = 0
) {
    val context = LocalContext.current
    val audioHelper = remember { AudioHelper(context) }
    DisposableEffect(Unit) {
        onDispose { audioHelper.release() }
    }

    var searchQuery by remember { mutableStateOf("") }

    // Distinct list to guarantee NO DUPLICATES in dictionary (Requirement #4)
    val distinctWords = remember(words) {
        words.distinctBy { it.georgian }
    }

    // Counts accounting (User Requirement: учет количества основных карточек и всех карточек)
    val totalMainCards = distinctWords.size
    val totalEncounteredSubCards = distinctWords.sumOf { word ->
        word.getEncounteredSubForms().size
    }
    val totalAllCards = totalMainCards + totalEncounteredSubCards

    // Sorted strictly in ALPHABETICAL order, NO category grouping (User Requirement #3)
    val filteredSortedWords = remember(distinctWords, searchQuery) {
        val query = searchQuery.trim().lowercase()
        distinctWords.filter { word ->
            query.isEmpty() ||
                    word.georgian.lowercase().contains(query) ||
                    word.russian.lowercase().contains(query) ||
                    word.transcription.lowercase().contains(query) ||
                    word.synonyms.lowercase().contains(query) ||
                    word.getSubForms().any { it.georgian.contains(query) || it.russian.lowercase().contains(query) }
        }.sortedBy { it.georgian }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dictionary_screen"),
        color = Color(0xFFF7F9FA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with strict Card Accounting (Requirement #1 & #3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E5E5),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
            ) {
                Text(
                    text = "Алфавитный словарь",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoDarkText
                )

                Text(
                    text = "Все слова и встреченные грамматические словоформы",
                    fontSize = 13.sp,
                    color = DuoSubtext,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Cards accounting chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "ОСНОВНЫХ КАРТОЧЕК",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoGreenDark
                            )
                            Text(
                                text = "$totalMainCards",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoGreenDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE1F5FE))
                            .border(1.dp, Color(0xFFB3E5FC), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "ВСЕГО КАРТОЧЕК",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue
                            )
                            Text(
                                text = "$totalAllCards",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск слова или словоформы...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Поиск", tint = DuoSubtext)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить", tint = DuoSubtext)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dict_search_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoBlue,
                        unfocusedBorderColor = Color(0xFFE0E5E8),
                        focusedContainerColor = Color(0xFFF7F9FA),
                        unfocusedContainerColor = Color(0xFFF7F9FA)
                    ),
                    singleLine = true
                )
            }

            // Word List or Empty State
            if (filteredSortedWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📖", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Словарь пуст" else "Ничего не найдено",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoDarkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Новые слова добавляются в словарь только через карточку 'Новое слово' в уроках. Пройдите первый урок в разделе 'Обучение'!"
                            } else {
                                "Попробуйте изменить поисковый запрос"
                            },
                            fontSize = 14.sp,
                            color = DuoSubtext,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
                ) {
                    items(filteredSortedWords, key = { it.georgian }) { word ->
                        WordItemCard(
                            word = word,
                            audioHelper = audioHelper
                        )
                    }
                }
            }
        }
    }
}

/**
 * Main dictionary card.
 * Displays word, transcription, Russian translation, part of speech badge, and audio.
 * When clicked, smoothly expands to show encountered subcards (subforms/conjugations/declensions).
 */
@Composable
fun WordItemCard(
    word: WordEntity,
    audioHelper: AudioHelper
) {
    var isExpanded by remember { mutableStateOf(false) }

    val encounteredSubForms = remember(word) { word.getEncounteredSubForms() }
    val allSubForms = remember(word) { word.getSubForms() }
    val hasSubForms = allSubForms.isNotEmpty()

    val partOfSpeechBadge = word.getPartOfSpeechEnum().labelRu

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (hasSubForms) {
                    isExpanded = !isExpanded
                }
            }
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Part of Speech + Subforms Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Part of Speech badge inside the card (Requirement #3)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F4F8))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = partOfSpeechBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoBlue
                    )
                }

                // Subforms counter badge
                if (hasSubForms) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (encounteredSubForms.isNotEmpty()) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Форм: ${encounteredSubForms.size}/${allSubForms.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (encounteredSubForms.isNotEmpty()) DuoGreenDark else DuoSubtext
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            tint = DuoSubtext,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Georgian script, Russian translation, Audio button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio button with clean speech synthesis
                IconButton(
                    onClick = { audioHelper.speakGeorgian(word.georgian, word.transcription) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DuoBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Озвучить слово",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Words
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.georgian,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoDarkText
                    )
                    Text(
                        text = "[ ${audioHelper.georgianToCyrillicPhonetics(word.georgian)} ]",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DuoBlue
                    )
                    Text(
                        text = word.russian,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoGreenDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Expandable Subcards (Only already encountered subforms!)
            AnimatedVisibility(visible = isExpanded && hasSubForms) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Встреченные словоформы и спряжения:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoDarkText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (encounteredSubForms.isEmpty()) {
                        Text(
                            text = "Словоформы этого слова ещё не встречались в уроках.",
                            fontSize = 12.sp,
                            color = DuoSubtext,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        encounteredSubForms.forEach { subForm ->
                            SubFormCardItem(
                                subForm = subForm,
                                onSpeak = { audioHelper.speakGeorgian(subForm.georgian, subForm.transcription) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual subcard item inside the expanded main card.
 */
@Composable
fun SubFormCardItem(
    subForm: WordSubForm,
    onSpeak: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7FBF8))
            .border(1.dp, Color(0xFFDCEFE0), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DuoGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Озвучить форму",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subForm.georgian,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoDarkText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "— ${subForm.russian}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DuoGreenDark
                    )
                }

                if (subForm.grammaticalNote.isNotBlank()) {
                    Text(
                        text = "(${subForm.grammaticalNote})",
                        fontSize = 11.sp,
                        color = DuoSubtext
                    )
                }
            }
        }
    }
}
