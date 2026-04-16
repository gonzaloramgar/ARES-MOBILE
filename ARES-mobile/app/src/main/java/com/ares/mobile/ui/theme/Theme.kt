package com.ares.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Create a Material3 color scheme from a design variant palette.
 */
private fun createColorScheme(palette: VariantColorPalette) = darkColorScheme(
    primary = palette.primary,
    onPrimary = palette.backgroundDeep,
    primaryContainer = palette.primaryDim,
    onPrimaryContainer = palette.textAccent,
    secondary = palette.primaryDim,
    onSecondary = palette.textPrimary,
    secondaryContainer = palette.surfaceVariantDark,
    onSecondaryContainer = palette.textAccent,
    tertiary = palette.textAccent,
    onTertiary = palette.backgroundDeep,
    background = palette.backgroundDeep,
    onBackground = palette.textPrimary,
    surface = palette.surfaceDark,
    onSurface = palette.textPrimary,
    surfaceVariant = palette.surfaceVariantDark,
    onSurfaceVariant = palette.textAccent,
    surfaceContainer = palette.surfaceDark,
    surfaceContainerHigh = palette.surfaceElevated,
    outline = palette.borderSubtle,
    outlineVariant = palette.borderGlow,
    error = palette.primary,
    onError = palette.backgroundDeep,
    scrim = Color(0x80000000),
)

/**
 * ARES Theme with support for multiple design variants.
 * Automatically applies the correct color scheme based on the variant.
 */
@Composable
fun AresTheme(
    variant: DesignVariant = DesignVariant.VIBRANT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = getVariantPalette(variant)
    val colorScheme = createColorScheme(palette)

    CompositionLocalProvider(
        LocalDesignVariant provides palette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AresTypography,
            content = content,
        )
    }
}

/**
 * Default Vibrant theme (for backward compatibility and main app).
 */
@Composable
fun AresThemeVibrant(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    AresTheme(
        variant = DesignVariant.VIBRANT,
        darkTheme = darkTheme,
        content = content,
    )
}

/**
 * Metallic theme variant (for refined, professional screens).
 */
@Composable
fun AresThemeMetallic(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    AresTheme(
        variant = DesignVariant.METALLIC,
        darkTheme = darkTheme,
        content = content,
    )
}
