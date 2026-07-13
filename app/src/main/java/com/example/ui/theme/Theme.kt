package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLoveColors = staticCompositionLocalOf { ThemePresets.RoseRomance }

@Composable
fun MyApplicationTheme(
  colors: LoveThemeColors = ThemePresets.RoseRomance,
  content: @Composable () -> Unit,
) {
  val isDark = colors.name.contains("Gothic") || colors.name.contains("Night")
  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = colors.primary,
      secondary = colors.secondary,
      tertiary = colors.tertiary,
      background = colors.bgLight,
      surface = colors.cardBg,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = colors.textDark,
      onSurface = colors.textDark
    )
  } else {
    lightColorScheme(
      primary = colors.primary,
      secondary = colors.secondary,
      tertiary = colors.tertiary,
      background = colors.bgLight,
      surface = colors.cardBg,
      onPrimary = Color.White,
      onSecondary = Color.White,
      onTertiary = Color.White,
      onBackground = colors.textDark,
      onSurface = colors.textDark
    )
  }

  CompositionLocalProvider(LocalLoveColors provides colors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
