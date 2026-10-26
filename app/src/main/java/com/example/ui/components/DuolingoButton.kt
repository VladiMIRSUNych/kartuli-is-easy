package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark

enum class ButtonStyle(
    val topColor: Color,
    val bottomColor: Color,
    val contentColor: Color
) {
    PRIMARY(DuoGreen, DuoGreenDark, Color.White),
    SECONDARY(Color(0xFF1CB0F6), Color(0xFF1899D6), Color.White),
    DANGER(Color(0xFFFF4B4B), Color(0xFFEA2B2B), Color.White),
    WARNING(Color(0xFFFFC800), Color(0xFFE5A500), Color(0xFF4B4B4B)),
    OUTLINE(Color.White, Color(0xFFE5E5E5), Color(0xFF1CB0F6)),
    DISABLED(Color(0xFFE5E5E5), Color(0xFFAFAFAF), Color(0xFFAFAFAF))
}

@Composable
fun DuolingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    enabled: Boolean = true,
    testTag: String = "duolingo_button",
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val activeStyle = if (enabled) style else ButtonStyle.DISABLED

    val depth: Dp = 4.dp
    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) depth else 0.dp,
        label = "button_depth"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp + depth)
            .testTag(testTag),
        contentAlignment = Alignment.TopCenter
    ) {
        // Bottom shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .offset(y = depth)
                .clip(shape)
                .background(activeStyle.bottomColor)
        )

        // Top button layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .offset(y = animatedOffset)
                .clip(shape)
                .background(activeStyle.topColor)
                .then(
                    if (style == ButtonStyle.OUTLINE) {
                        Modifier.border(2.dp, Color(0xFFE5E5E5), shape)
                    } else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Box(modifier = Modifier.padding(end = 8.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = activeStyle.contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
