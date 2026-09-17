package com.bakeerp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = BrandSecondary,
    onPrimaryContainer = OnSurfaceLight,
    secondary = BrandSecondary,
    onSecondary = OnSurfaceLight,
    tertiary = BrandTertiary,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = BrandSecondary,
    onPrimary = OnSurfaceDark,
    primaryContainer = BrandPrimaryDark,
    onPrimaryContainer = OnSurfaceDark,
    secondary = BrandPrimary,
    onSecondary = OnSurfaceDark,
    tertiary = BrandTertiary,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = DangerRed
)

@Composable
fun BakeErpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BakeErpTypography,
        content = content
    )
}