package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.DuoDarkText

@Composable
fun WordBubble(
    word: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUsed: Boolean = false,
    fontSize: Int = 16,
    testTag: String = "word_bubble"
) {
    val shape = RoundedCornerShape(12.dp)

    if (isUsed) {
        // Disabled empty placeholder
        Box(
            modifier = modifier
                .padding(3.dp)
                .clip(shape)
                .background(Color(0xFFE5E5E5))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word,
                color = Color.Transparent,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Active clickable 3D bubble
        Box(
            modifier = modifier
                .padding(3.dp)
                .testTag(testTag),
            contentAlignment = Alignment.TopCenter
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 3.dp)
                    .clip(shape)
                    .background(Color(0xFFCCD4DA))
            )

            // Content
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFE0E5E8), shape)
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = word,
                    color = DuoDarkText,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
