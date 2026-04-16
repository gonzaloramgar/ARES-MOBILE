package com.ares.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.ui.theme.NeonRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.role == MessageRole.User
    val isAssistant = message.role == MessageRole.Assistant || message.role == MessageRole.Tool

    val userShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    val aresShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    val shape = if (isUser) userShape else aresShape

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            if (isAssistant) {
                Text(
                    text = "ARES",
                    color = colors.primary.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                )
            }

            Box(
                modifier = Modifier
                    .then(
                        if (isAssistant) Modifier.aresGlow(colors.primary.copy(alpha = 0.06f), 12.dp)
                        else Modifier
                    )
                    .background(if (isUser) colors.surface else colors.surfaceVariant, shape)
                    .border(1.dp, if (isUser) colors.outline else colors.outlineVariant, shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = message.content,
                    color = if (isUser) colors.onSurface else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                    ),
                )
            }

            // Timestamp + tool badge row
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!message.toolName.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .background(colors.surfaceVariant, RoundedCornerShape(6.dp))
                            .border(1.dp, colors.outlineVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("⚙", fontSize = 8.sp, color = colors.primary.copy(alpha = 0.7f))
                        Text(
                            text = message.toolName.lowercase(),
                            fontSize = 8.sp,
                            color = colors.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
                Text(
                    text = timeFmt.format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    color = colors.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

fun Modifier.aresGlow(color: Color, radius: Dp): Modifier = drawBehind {
    val rPx = radius.toPx()
    val layers = 7
    repeat(layers) { i ->
        val spread = rPx * (i + 1) / layers.toFloat()
        val alpha = color.alpha * (1f - i.toFloat() / layers) * 0.35f
        drawRoundRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(-spread, -spread),
            size = Size(size.width + spread * 2f, size.height + spread * 2f),
            cornerRadius = CornerRadius(18.dp.toPx() + spread, 18.dp.toPx() + spread),
        )
    }
}
