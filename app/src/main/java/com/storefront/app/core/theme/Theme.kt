package com.storefront.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    secondary = TerracottaSecondary,
    onSecondary = Color.White,
    background = MistBackground,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText,
    surfaceVariant = MistVariant,
    onSurfaceVariant = InkText
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = NightBackground,
    secondary = TerracottaSecondaryDark,
    onSecondary = NightBackground,
    background = NightBackground,
    onBackground = FrostText,
    surface = NightSurface,
    onSurface = FrostText,
    surfaceVariant = NightVariant,
    onSurfaceVariant = FrostText
)

@Composable
fun StorefrontTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
