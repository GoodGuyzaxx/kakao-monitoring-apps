package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CacaoGreenPrimaryDark,
    onPrimary = CacaoGreenOnPrimaryDark,
    primaryContainer = CacaoGreenContainerDark,
    onPrimaryContainer = CacaoGreenOnContainerDark,
    secondary = CacaoSoilSecondaryDark,
    onSecondary = CacaoSoilOnSecondaryDark,
    secondaryContainer = CacaoSoilContainerDark,
    onSecondaryContainer = CacaoSoilOnContainerDark,
    tertiary = WaterAquaTertiaryDark,
    onTertiary = WaterAquaOnTertiaryDark,
    tertiaryContainer = WaterAquaContainerDark,
    onTertiaryContainer = WaterAquaOnContainerDark,
    background = WarmDarkBackground,
    onBackground = WarmDarkOnSurface,
    surface = WarmDarkSurface,
    onSurface = WarmDarkOnSurface,
    surfaceVariant = WarmDarkSurfaceVariant,
    onSurfaceVariant = WarmDarkOnSurfaceVariant,
    outline = WarmDarkOutline,
    outlineVariant = WarmDarkOutlineVariant,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CacaoGreenPrimary,
    onPrimary = CacaoGreenOnPrimary,
    primaryContainer = CacaoGreenContainer,
    onPrimaryContainer = CacaoGreenOnContainer,
    secondary = CacaoSoilSecondary,
    onSecondary = CacaoSoilOnSecondary,
    secondaryContainer = CacaoSoilContainer,
    onSecondaryContainer = CacaoSoilOnContainer,
    tertiary = WaterAquaTertiary,
    onTertiary = WaterAquaOnTertiary,
    tertiaryContainer = WaterAquaContainer,
    onTertiaryContainer = WaterAquaOnContainer,
    background = WarmLightBackground,
    onBackground = WarmLightOnSurface,
    surface = WarmLightSurface,
    onSurface = WarmLightOnSurface,
    surfaceVariant = WarmLightSurfaceVariant,
    onSurfaceVariant = WarmLightOnSurfaceVariant,
    outline = WarmLightOutline,
    outlineVariant = WarmLightOutlineVariant,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted nature palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
