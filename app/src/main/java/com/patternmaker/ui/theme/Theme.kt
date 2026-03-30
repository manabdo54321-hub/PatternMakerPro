package com.patternmaker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B4F72),
    secondary = Color(0xFF2874A6),
    tertiary = Color(0xFF148F77),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun PatternMakerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
