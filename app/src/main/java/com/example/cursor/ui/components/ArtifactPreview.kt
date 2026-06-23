package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ArtifactWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ArtifactPreview(
  artifact: ArtifactWorkbench,
  modifier: Modifier = Modifier,
) {
  WorkbenchCard(title = "Artifact preview", eyebrow = "Runnable output", modifier = modifier) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = CursorShape.Preview,
      color = CursorColors.DarkPanel,
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
      Column(Modifier.padding(CursorSpacing.Lg), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2F8F75),
          ) {}
          Spacer(Modifier.size(CursorSpacing.Md))
          Column(Modifier.weight(1f)) {
            Text(artifact.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(artifact.subtitle, color = Color.White.copy(alpha = 0.68f), style = MaterialTheme.typography.labelMedium)
          }
          CursorChip("Run")
        }
        LinearProgressIndicator(
          progress = { artifact.progress },
          modifier = Modifier.fillMaxWidth(),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.14f),
        )
        artifact.previewLines.forEachIndexed { index, line ->
          Text(
            text = line,
            color = if (index == 1) Color.White else Color.White.copy(alpha = 0.72f),
            style = if (index == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
          )
        }
      }
    }
  }
}
