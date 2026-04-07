package com.ares.mobile.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.SurfaceVariantDark

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")

    Box(
        modifier = modifier
            .background(SurfaceVariantDark, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
            .border(1.dp, NeonRedBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(0, 200, 400).forEach { delayMs ->
                val scale by transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.4f at 0
                            1f at (300 + delayMs)
                            0.4f at (600 + delayMs)
                            0.4f at 1200
                        },
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "dot_$delayMs",
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(scale)
                        .background(NeonRed, CircleShape),
                )
            }
        }
    }
}
