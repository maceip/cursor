package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ArtifactPreviewState
import com.example.cursor.model.QuizOption
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ArtifactPreview(
  state: ArtifactPreviewState,
  modifier: Modifier = Modifier,
) {
  CursorCard(modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      Box(
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(CursorColors.DarkArtifact)
          .padding(CursorSpacing.Lg),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
          Row {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF2F8F75)))
            Column(Modifier.padding(horizontal = CursorSpacing.Md)) {
              Text(state.appName, color = Color.White, fontWeight = FontWeight.SemiBold)
              Text(state.subtitle, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.08f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))) {
              Text("Run", color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
            }
          }
          Text(state.progressLabel, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
          LinearProgressIndicator(
            progress = { 0.45f },
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.15f),
          )
          Text(state.question, color = Color.White, style = MaterialTheme.typography.titleMedium)
          state.options.forEach { option -> QuizOptionRow(option) }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        CursorChip("Open artifact")
        CursorChip("Iterate")
      }
    }
  }
}

@Composable
private fun QuizOptionRow(option: QuizOption) {
  val stroke = if (option.selected) Color(0xFF3DBB9A) else Color.White.copy(alpha = 0.10f)
  Row(
    Modifier
      .fillMaxWidth()
      .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
      .border(1.dp, stroke, RoundedCornerShape(10.dp))
      .padding(CursorSpacing.Md),
  ) {
    Text(option.label, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
    Text("  ${option.text}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
  }
}
