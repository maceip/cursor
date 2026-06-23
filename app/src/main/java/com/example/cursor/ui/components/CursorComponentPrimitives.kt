package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CursorCard(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = CursorColors.Card,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = 1.dp,
  ) {
    Box(Modifier.padding(CursorSpacing.Lg)) { content() }
  }
}

@Composable
fun CursorChip(
  text: String,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
) {
  val borderColor = if (selected) CursorColors.CursorBlue.copy(alpha = 0.35f) else CursorColors.Stroke
  val background = if (selected) Color(0xFFEAF3FF) else CursorColors.Card
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = background,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelLarge,
      color = if (selected) CursorColors.CursorBlue else CursorColors.Ink,
    )
  }
}

@Composable
fun CursorLogo(modifier: Modifier = Modifier) {
  Box(
    modifier
      .size(28.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(Brush.linearGradient(listOf(Color.Black, Color(0xFF777777))))
      .border(1.dp, Color.Black.copy(alpha = 0.18f), RoundedCornerShape(6.dp)),
  )
}

@Composable
fun CursorAvatar(label: String, modifier: Modifier = Modifier) {
  Box(
    modifier.size(32.dp).clip(CircleShape).background(CursorColors.Ink),
    contentAlignment = Alignment.Center,
  ) {
    if (label == "Cursor") CursorLogo(Modifier.size(17.dp)) else Text(label.take(2), color = Color.White, style = MaterialTheme.typography.labelMedium)
  }
}

@Composable
fun MetaRow(
  label: String,
  trailing: String,
  modifier: Modifier = Modifier,
) {
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    Text(label, color = CursorColors.Muted, style = MaterialTheme.typography.labelLarge)
    Text("  $trailing", color = CursorColors.Muted, style = MaterialTheme.typography.labelMedium)
  }
}
