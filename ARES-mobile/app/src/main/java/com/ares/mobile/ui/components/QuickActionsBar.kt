package com.ares.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.viewmodel.QuickActionItem

private val actionEmojis = mapOf(
    "Portapapeles" to "📋",
    "Ubicación" to "📍",
    "Leer en voz" to "🔊",
    "Alarma 10m" to "⏰",
    "Cámara" to "📷",
)

@Composable
fun QuickActionsBar(
    actions: List<QuickActionItem>,
    onActionClick: (QuickActionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        actions.forEach { action ->
            val emoji = actionEmojis[action.label] ?: "✦"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark, RoundedCornerShape(16.dp))
                    .border(1.dp, NeonRedBorder, RoundedCornerShape(16.dp))
                    .clickable { onActionClick(action) }
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(emoji, fontSize = 12.sp)
                Text(action.label, color = NeonRed, fontSize = 11.sp)
            }
        }
    }
}
