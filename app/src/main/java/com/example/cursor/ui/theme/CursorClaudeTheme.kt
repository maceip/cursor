package com.example.cursor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CursorLightColorScheme =
  lightColorScheme(
    background = CursorColors.Cream,
    surface = CursorColors.Card,
    surfaceVariant = CursorColors.Soft,
    primary = CursorColors.Ink,
    onPrimary = Color.White,
    secondary = CursorColors.CursorBlue,
    onSecondary = Color.White,
    tertiary = CursorColors.Rust,
    onBackground = CursorColors.Ink,
    onSurface = CursorColors.Ink,
    outline = CursorColors.Stroke,
  )

private val CursorTypography =
  Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp),
  )

@Composable
fun CursorClaudeTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CursorLightColorScheme,
    typography = CursorTypography,
    shapes = CursorShapes,
    content = content,
  )
}
