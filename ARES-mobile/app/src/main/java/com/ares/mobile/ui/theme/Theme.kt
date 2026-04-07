package com.ares.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AresColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = BackgroundDeep,
    primaryContainer = NeonRedDim,
    onPrimaryContainer = TextAres,
    secondary = NeonRedDim,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = TextAres,
    tertiary = TextAres,
    onTertiary = BackgroundDeep,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextAres,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceElevated,
    outline = BorderSubtle,
    outlineVariant = BorderGlow,
    error = NeonRed,
    onError = BackgroundDeep,
    scrim = Color(0x80000000),
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
