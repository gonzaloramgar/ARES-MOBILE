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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.ui.theme.BorderGlow
import com.ares.mobile.ui.theme.BorderSubtle
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.ui.theme.SurfaceVariantDark
import com.ares.mobile.ui.theme.TextAres
import com.ares.mobile.ui.theme.TextMuted
import com.ares.mobile.ui.theme.TextPrimary

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == MessageRole.User
    val isAssistant = message.role == MessageRole.Assistant || message.role == MessageRole.Tool

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            if (isAssistant) {
                Text(
                    text = "ARES",
                    color = NeonRed.copy(alpha = 0.45f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                )
            }

            val userShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            val aresShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
            val shape = if (isUser) userShape else aresShape

            Box(
                modifier = Modifier
                    .then(
                        if (isAssistant) Modifier.aresGlow(NeonRed.copy(alpha = 0.07f), 14.dp)
                        else Modifier
                    )
                    .background(
                        color = if (isUser) SurfaceDark else SurfaceVariantDark,
                        shape = shape,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) BorderSubtle else NeonRedBorder,
                        shape = shape,
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = message.content,
                    color = if (isUser) TextPrimary else TextAres,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            }

            if (!message.toolName.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .background(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .border(1.dp, NeonRedBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚙", fontSize = 9.sp, color = NeonRed)
                    Text(
                        text = message.toolName.lowercase(),
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }
    }
}

fun Modifier.aresGlow(color: Color, radius: Dp): Modifier = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(radius.toPx(), 0f, 0f, color.toArgb())
            maskFilter = BlurMaskFilter(radius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height, 18.dp.toPx(), 18.dp.toPx(), paint,
        )
    }
}
