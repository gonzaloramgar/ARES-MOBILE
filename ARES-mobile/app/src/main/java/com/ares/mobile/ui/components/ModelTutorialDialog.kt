package com.ares.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ares.mobile.ui.theme.NeonRed

/**
 * Tutorial step for model configuration flow.
 */
data class TutorialStep(
    val title: String,
    val description: String,
    val details: List<String> = emptyList(),
)

/**
 * Interactive tutorial dialog for model setup and configuration.
 */
@Composable
fun ModelTutorialDialog(
    isVisible: Boolean,
    title: String,
    steps: List<TutorialStep>,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Column {
                    Text(
                        text = "📚 Tutorial",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonRed,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Steps
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    steps.forEachIndexed { index, step ->
                        TutorialStepCard(
                            stepNumber = index + 1,
                            step = step,
                        )
                    }
                }

                // Action button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Entendido", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TutorialStepCard(
    stepNumber: Int,
    step: TutorialStep,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Step header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Paso $stepNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonRed,
                    letterSpacing = 0.8.sp,
                )
            }

            // Title
            Text(
                text = step.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )

            // Description
            Text(
                text = step.description,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                lineHeight = 16.sp,
            )

            // Details (if any)
            if (step.details.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    step.details.forEach { detail ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = NeonRed,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = detail,
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant,
                                lineHeight = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
