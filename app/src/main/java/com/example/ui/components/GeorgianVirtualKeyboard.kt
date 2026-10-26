package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InitialData
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeorgianVirtualKeyboard(
    onLetterClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSpaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8ECEF))
            .padding(8.dp)
            .testTag("georgian_virtual_keyboard")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Грузинские буквы (მხედრული):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666),
                modifier = Modifier.padding(start = 4.dp)
            )

            // Special actions row (Space, Backspace)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Space
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp))
                        .clickable { onSpaceClick() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "␣ Пробел",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoDarkText
                    )
                }

                // Backspace
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp))
                        .clickable { onBackspaceClick() }
                        .testTag("keyboard_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Удалить",
                        tint = Color(0xFFFF4B4B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Letter grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 11
        ) {
            InitialData.GEORGIAN_KEYBOARD_LETTERS.forEach { letter ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(width = 30.dp, height = 36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCCD4DA), RoundedCornerShape(6.dp))
                        .clickable { onLetterClick(letter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DuoDarkText
                    )
                }
            }
        }
    }
}
