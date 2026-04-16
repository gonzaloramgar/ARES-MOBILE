package com.ares.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design variant system for ARES Mobile.
 * Supports multiple aesthetic themes: Vibrant (neon red) and Metallic (platinum).
 */

enum class DesignVariant {
    VIBRANT,
    METALLIC,
}

/**
 * Color palette for a specific design variant.
 * Each variant defines the complete color scheme.
 */
data class VariantColorPalette(
    // Primary colors
    val primary: Color,
    val primaryDim: Color,
    val primaryGlow: Color,
    val primaryBorder: Color,
    // Backgrounds
    val backgroundDeep: Color,
    val surfaceDark: Color,
    val surfaceVariantDark: Color,
    val surfaceElevated: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textAccent: Color,
    val textMuted: Color,
    // Borders
    val borderSubtle: Color,
    val borderGlow: Color,
)

/**
 * Get the color palette for a specific design variant.
 */
fun getVariantPalette(variant: DesignVariant): VariantColorPalette = when (variant) {
    DesignVariant.VIBRANT -> VariantColorPalette(
        primary = NeonRed,
        primaryDim = NeonRedDim,
        primaryGlow = NeonRedGlow,
        primaryBorder = NeonRedBorder,
        backgroundDeep = BackgroundDeep,
        surfaceDark = SurfaceDark,
        surfaceVariantDark = SurfaceVariantDark,
        surfaceElevated = SurfaceElevated,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textAccent = TextAres,
        textMuted = TextMuted,
        borderSubtle = BorderSubtle,
        borderGlow = BorderGlow,
    )
    DesignVariant.METALLIC -> VariantColorPalette(
        primary = MetallicPrimary,
        primaryDim = MetallicPrimaryDim,
        primaryGlow = MetallicPrimaryGlow,
        primaryBorder = MetallicPrimaryBorder,
        backgroundDeep = MetallicBackgroundDeep,
        surfaceDark = MetallicSurfaceDark,
        surfaceVariantDark = MetallicSurfaceVariantDark,
        surfaceElevated = MetallicSurfaceElevated,
        textPrimary = MetallicTextPrimary,
        textSecondary = MetallicTextSecondary,
        textAccent = MetallicTextAccent,
        textMuted = MetallicTextMuted,
        borderSubtle = MetallicBorderSubtle,
        borderGlow = MetallicBorderGlow,
    )
}

/**
 * Get design variant from tab name.
 * Maps tab identifiers to their assigned design variant.
 */
fun getDesignVariantForTab(tabName: String): DesignVariant = when (tabName.lowercase()) {
    "chat" -> DesignVariant.VIBRANT
    "memory" -> DesignVariant.METALLIC
    "tasks" -> DesignVariant.METALLIC
    "settings" -> DesignVariant.METALLIC
    else -> DesignVariant.VIBRANT // Default
}
