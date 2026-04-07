package com.ares.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AresColorScheme = darkColorScheme(
    primary = NeonRed,
    secondary = NeonRedDim,
    tertiary = TextAres,
    background = BackgroundDeep,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = BackgroundDeep,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextAres,
    outline = BorderSubtle,
)

@Composable
fun AresTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AresColorScheme,
        typography = AresTypography,
        content = content,
    )
}