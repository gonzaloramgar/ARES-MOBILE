package com.ares.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for accessing the current design variant colors.
 * Provides easy access to variant-specific colors throughout the UI hierarchy.
 */
val LocalDesignVariant = staticCompositionLocalOf<VariantColorPalette> {
    error("LocalDesignVariant not provided. Ensure you're using AresTheme.")
}

/**
 * Extension property for quick access to variant colors from any Composable.
 * Usage: val colors = AresVariantColors
 */
val AresVariantColors: VariantColorPalette
    @Composable
    get() = LocalDesignVariant.current
