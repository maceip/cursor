package com.example.cursor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CursorColors {
  val Cream = Color(0xFFFAF8F3)
  val Card = Color(0xFFFEFCF8)
  val Soft = Color(0xFFF2F0EA)
  val Stroke = Color(0xFFE1DED6)
  val Ink = Color(0xFF171717)
  val Muted = Color(0xFF77746D)
  val CursorBlue = Color(0xFF2F7DF6)
  val CodeGreen = Color(0xFFEAF5E8)
  val CodeRed = Color(0xFFFFECE8)
  val Rust = Color(0xFFC85F36)
  val DarkArtifact = Color(0xFF191919)
}

object CursorSpacing {
  val Xs: Dp = 4.dp
  val Sm: Dp = 8.dp
  val Md: Dp = 12.dp
  val Lg: Dp = 16.dp
  val Xl: Dp = 24.dp
  val Xxl: Dp = 32.dp
}

object CursorElevation {
  val Card: Dp = 1.dp
  val Dock: Dp = 4.dp
}

val CursorShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
  )
