package com.example.cursor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CursorColorScheme =
  lightColorScheme(
    background = CursorColors.Cream,
    onBackground = CursorColors.Ink,
    surface = CursorColors.Surface,
    surfaceVariant = CursorColors.SurfaceSoft,
    onSurface = CursorColors.Ink,
    primary = CursorColors.Ink,
    onPrimary = Color.White,
    secondary = CursorColors.Blue,
    onSecondary = Color.White,
    tertiary = CursorColors.Rust,
    outline = CursorColors.Stroke,
  )

private val CursorTypography =
  Typography(
    displaySmall =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 19.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
      ),
    labelMedium =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
      ),
  )

private val CursorShapes =
  Shapes(
    small = CursorShape.Chip,
    medium = CursorShape.Card,
    large = CursorShape.Dock,
  )

@Composable
fun CursorClaudeTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = CursorColorScheme,
    typography = CursorTypography,
    shapes = CursorShapes,
    content = content,
  )
}
