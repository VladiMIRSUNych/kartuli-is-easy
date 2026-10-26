package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.data.UserStatsEntity
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoDarkText
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoSubtext

@Composable
fun HeaderStatsBar(
    stats: UserStatsEntity?,
    totalWordsCount: Int = 0,
    completedTodayCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🇬🇪 Kartuli",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = DuoDarkText
            )
            Text(
                text = "Ежедневная практика грузинского",
                fontSize = 12.sp,
                color = DuoSubtext
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Words learned chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("stat_words_count")
            ) {
                Text(text = "📚", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$totalWordsCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoDarkText
                )
            }

            // Lessons completed today chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("stat_lessons_today")
            ) {
                Text(
                    text = if (completedTodayCount >= 3) "✓ 3/3 готово" else "$completedTodayCount/3 урока",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoGreenDark
                )
            }
        }
    }
}
