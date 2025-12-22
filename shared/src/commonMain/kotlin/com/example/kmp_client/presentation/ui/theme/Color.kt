package com.example.kmp_client.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Светлая тема
val LightPrimary = Color(0xFF3171B8)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD6E3FF)
val LightOnPrimaryContainer = Color(0xFF001C3B)
val LightSecondary = Color(0xFFE9980D)
val LightOnSecondary = Color(0xFF000000)
val LightSecondaryContainer = Color(0xFF3171B8).copy(alpha = 0.3f)
val LightOnSecondaryContainer = Color(0xFF3171B8)
val LightTertiary = Color(0xFFD7D9C875)
val LightOnTertiaryContainer = Color(0xFF642B5D)
val LightError = Color(0xFF3171B8)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFD3D0D2)
val LightOnErrorContainer = Color(0xFF3171B8)
val LightBackground = Color(0xFFFAFAFA)
val LightOnBackground = Color(0xFF000000)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF000000)
val LightSurfaceVariant = Color(0xFFE0E2EC)
val LightOnSurfaceVariant = Color(0xFF44474E)
val LightOutline = Color(0xFF74777F)
val LightOutlineVariant = Color(0xFFC4C6CF)

// Темная тема
val DarkPrimary = Color(0xFF24374C)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkPrimaryContainer = Color(0xFF004886)
val DarkOnPrimaryContainer = Color(0xFFD6E3FF)
val DarkSecondary = Color(0xFFE9980D)
val DarkOnSecondary = Color(0xFF000000)
val DarkSecondaryContainer = Color(0xFFE9980D).copy(alpha = 0.3f)
val DarkOnSecondaryContainer = Color(0xFFFFDDB9)
val DarkTertiary = Color(0xFFD7D9C875)
val DarkOnTertiary = Color(0xFF44293F)
val DarkTertiaryContainer = Color(0xFF5D4057)
val DarkOnTertiaryContainer = Color(0xFF1E5492)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkBackground = Color(0xFF18222E)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkSurface = Color(0xFF1F2C3A)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkSurfaceVariant = Color(0xFF44474E)
val DarkOnSurfaceVariant = Color(0xFFC4C6CF)
val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF44474E)

val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary
)
val DarkColors = darkColorScheme(
    primary = DarkSecondary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)