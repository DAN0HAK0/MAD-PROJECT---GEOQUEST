package com.dan.mad_project_geoquest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Dark Theme: Deep explorer's journal at night ───────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Gold,          // CTA buttons, FABs, active icons
    onPrimary        = DarkBrown,     // Text/icon on Gold
    primaryContainer = Brown,         // Chips, selected states
    onPrimaryContainer = Cream,       // Text inside primaryContainer

    secondary        = Sand,          // Secondary actions
    onSecondary      = DarkBrown,
    secondaryContainer = Leather,     // Tonal buttons, filter chips
    onSecondaryContainer = Cream,

    tertiary         = Leather,       // Accent / highlight colour
    onTertiary       = Cream,
    tertiaryContainer = DarkBrown,
    onTertiaryContainer = Sand,

    background       = DarkBrown,     // Root screen background
    onBackground     = Cream,         // Body text on background

    surface          = Brown,         // Cards, sheets, dialogs
    onSurface        = Cream,         // Text on cards
    surfaceVariant   = DarkBrown,     // Input fields, divider areas
    onSurfaceVariant = Sand,          // Hint/secondary text on inputs

    outline          = Sand,          // Borders, dividers
    outlineVariant   = Leather,       // Subtle dividers

    error            = MapRed,
    onError          = Cream,
    errorContainer   = DarkBrown,
    onErrorContainer = MapRed,

    inverseSurface        = Cream,
    inverseOnSurface      = DarkBrown,
    inversePrimary        = DarkBrown,

    scrim            = DarkBrown,
)

// ─── Light Theme: Aged parchment in afternoon sunlight ──────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = DarkGold,      // Richer gold so it pops on light bg
    onPrimary        = Cream,
    primaryContainer = PaleParchment, // Subtle tonal container
    onPrimaryContainer = DarkBrown,

    secondary        = Leather,       // Warm terracotta for secondary
    onSecondary      = Cream,
    secondaryContainer = Sand,        // Soft sand chips / tonal buttons
    onSecondaryContainer = DarkBrown,

    tertiary         = MapRed,        // Compass-red accent
    onTertiary       = Cream,
    tertiaryContainer = PaleParchment,
    onTertiaryContainer = DarkBrown,

    background       = Parchment,     // Warm off-white parchment
    onBackground     = DarkBrown,     // Dark ink text

    surface          = Cream,         // Cards, sheets
    onSurface        = DarkBrown,
    surfaceVariant   = PaleParchment, // Input backgrounds
    onSurfaceVariant = Brown,         // Hint text in inputs

    outline          = Leather,       // Borders — warm, not harsh grey
    outlineVariant   = Sand,          // Subtle dividers

    error            = MapRed,
    onError          = Cream,
    errorContainer   = PaleParchment,
    onErrorContainer = MapRed,

    inverseSurface        = DarkBrown,
    inverseOnSurface      = Cream,
    inversePrimary        = Gold,

    scrim            = DarkBrown,
)

@Composable
fun MadProjectGEOQUESTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Tint the status bar to match the theme background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}