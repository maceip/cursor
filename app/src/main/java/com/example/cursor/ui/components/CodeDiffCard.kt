package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.cursor.model.CodeReviewWorkbench
import com.example.cursor.model.DiffLineKind
import com.example.cursor.model.DiffLineRenderModel
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodeDiffCard(
  codeReview: CodeReviewWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(
      title = "Plan",
      icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
      trailing = { StatusPill("In progress") },
    ) {
      Checklist(codeReview.plan)
    }
    codeReview.diff.files.forEach { file ->
      WorkbenchCard(
        title = file.filePath,
        icon = Icons.Outlined.Code,
        trailing = { Text("Suggested change", style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted) },
      ) {
        Column(
          Modifier
            .fillMaxWidth()
            .background(CursorColors.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, CursorColors.Stroke.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(CursorSpacing.Xs),
          verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
          file.lines.forEach { line -> DiffLine(line) }
        }
      }
    }
    WorkbenchCard(title = "Relevant files", icon = Icons.AutoMirrored.Outlined.Article) {
      FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        codeReview.files.forEach { file ->
          androidx.compose.material3.Surface(
            shape = CursorShape.CardSmall,
            color = CursorColors.Surface,
            border = BorderStroke(1.dp, CursorColors.Stroke),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
            ) {
              Text("TS", style = MaterialTheme.typography.labelMedium, color = CursorColors.Blue)
              Column(Modifier.width(92.dp)) {
                Text(file, style = MaterialTheme.typography.labelMedium)
                Text(if (file == "app.tsx") "src" else "src/hooks", style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
              }
              androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
            }
          }
        }
      }
    }
    WorkbenchCard(
      title = codeReview.readyTitle,
      icon = Icons.Outlined.AutoAwesome,
      trailing = { CursorChip("Review changes") },
    ) {
      Text(codeReview.readyBody, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted)
    }
  }
}

@Composable
private fun DiffLine(line: DiffLineRenderModel) {
  val background =
    when (line.kind) {
      DiffLineKind.Addition -> CursorColors.GreenSoft
      DiffLineKind.Deletion -> CursorColors.Red
      DiffLineKind.Header -> CursorColors.DarkPanel.copy(alpha = 0.08f)
      DiffLineKind.Context -> Color.Transparent
    }
  Row(
    Modifier
      .fillMaxWidth()
      .background(background, RoundedCornerShape(8.dp))
      .padding(horizontal = 7.dp, vertical = 3.dp)
  ) {
    Text(
      text = line.oldNumber?.toString().orEmpty(),
      modifier = Modifier.width(24.dp),
      style = MaterialTheme.typography.labelMedium,
      color = CursorColors.Muted,
      fontFamily = FontFamily.Monospace,
    )
    Text(
      text = line.newNumber?.toString().orEmpty(),
      modifier = Modifier.width(24.dp),
      style = MaterialTheme.typography.labelMedium,
      color = CursorColors.Muted,
      fontFamily = FontFamily.Monospace,
    )
    Text(
      text = line.marker,
      modifier = Modifier.width(15.dp),
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
    )
    Spacer(Modifier.width(CursorSpacing.Sm))
    Text(
      text = line.text,
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.weight(1f),
    )
  }
}
