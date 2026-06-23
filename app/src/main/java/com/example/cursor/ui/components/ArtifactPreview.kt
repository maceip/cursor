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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ArtifactChoice
import com.example.cursor.model.ArtifactWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ArtifactPreview(
  artifact: ArtifactWorkbench,
  modifier: Modifier = Modifier,
) {
  WorkbenchCard(title = "Artifact preview", icon = Icons.Outlined.ViewInAr, modifier = modifier) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = CursorShape.Preview,
      color = CursorColors.DarkPanel,
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
      Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(42.dp).background(Color(0xFF2F8F75), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(Modifier.size(CursorSpacing.Md))
          Column(Modifier.weight(1f)) {
            Text(artifact.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
            Text(artifact.subtitle, color = Color.White.copy(alpha = 0.68f), style = MaterialTheme.typography.labelMedium)
          }
          Surface(shape = CursorShape.Chip, color = Color.Transparent, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Text("Connect", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
          }
        }
        Text(artifact.questionLabel, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
          progress = { artifact.progress },
          modifier = Modifier.fillMaxWidth().height(5.dp),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.12f),
        )
        Surface(shape = CursorShape.Preview, color = CursorColors.DarkPanelSoft.copy(alpha = 0.88f)) {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
            Surface(shape = CursorShape.Chip, color = Color(0xFF1E6E57)) {
              Text(artifact.category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color(0xFF77E1C1), style = MaterialTheme.typography.labelMedium)
            }
            Text(
              artifact.question,
              color = Color.White,
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
            )
            artifact.choices.forEach { choice -> ArtifactChoiceRow(choice) }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(Modifier.size(10.dp).background(CursorColors.Green, CircleShape))
              Spacer(Modifier.size(CursorSpacing.Sm))
              Text(artifact.footerStart, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
              Spacer(Modifier.weight(1f))
              Text(artifact.footerEnd, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      CursorChip("Open artifact", icon = Icons.AutoMirrored.Outlined.OpenInNew)
      CursorChip("Iterate", icon = Icons.Outlined.Cached)
    }
  }
}

@Composable
private fun ArtifactChoiceRow(choice: ArtifactChoice) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Color.White.copy(alpha = 0.06f), CursorShape.CardSmall)
        .border(
          1.dp,
          if (choice.selected) Color(0xFF33B891) else Color.White.copy(alpha = 0.1f),
          CursorShape.CardSmall,
        )
        .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.size(26.dp).background(Color.White.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
      Text(choice.label, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium)
    }
    Spacer(Modifier.size(CursorSpacing.Md))
    Text(choice.text, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    if (choice.selected) {
      Box(
        modifier = Modifier.size(18.dp).border(2.dp, Color(0xFF42CBA3), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Box(Modifier.size(8.dp).background(Color(0xFF42CBA3), CircleShape))
      }
    }
  }
}
