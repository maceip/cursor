package com.example.cursor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CodeDiffCard(
  codeReview: CodeReviewWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(title = "Plan", eyebrow = "Code review") { Checklist(codeReview.plan) }
    codeReview.diff.files.forEach { file ->
      WorkbenchCard(title = file.filePath, eyebrow = "+${file.addedLineCount} / -${file.removedLineCount}") {
        Column(
          Modifier
            .fillMaxWidth()
            .background(CursorColors.SurfaceSoft, RoundedCornerShape(12.dp))
            .padding(CursorSpacing.Sm),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          file.lines.forEach { line -> DiffLine(line) }
        }
      }
    }
  }
}

@Composable
private fun DiffLine(line: DiffLineRenderModel) {
  val background =
    when (line.kind) {
      DiffLineKind.Addition -> CursorColors.Green
      DiffLineKind.Deletion -> CursorColors.Red
      DiffLineKind.Header -> CursorColors.DarkPanel.copy(alpha = 0.08f)
      DiffLineKind.Context -> Color.Transparent
    }
  Row(
    Modifier
      .fillMaxWidth()
      .background(background, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 5.dp)
  ) {
    Text(
      text = line.oldNumber?.toString().orEmpty(),
      modifier = Modifier.width(30.dp),
      style = MaterialTheme.typography.labelMedium,
      color = CursorColors.Muted,
      fontFamily = FontFamily.Monospace,
    )
    Text(
      text = line.newNumber?.toString().orEmpty(),
      modifier = Modifier.width(30.dp),
      style = MaterialTheme.typography.labelMedium,
      color = CursorColors.Muted,
      fontFamily = FontFamily.Monospace,
    )
    Text(
      text = line.marker,
      modifier = Modifier.width(18.dp),
      style = MaterialTheme.typography.labelMedium,
      fontFamily = FontFamily.Monospace,
    )
    Spacer(Modifier.width(CursorSpacing.Sm))
    Text(
      text = line.text,
      style = MaterialTheme.typography.bodyMedium,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.weight(1f),
    )
  }
}
