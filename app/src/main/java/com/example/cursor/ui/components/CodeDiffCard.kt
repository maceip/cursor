package com.example.cursor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.diff.DiffLineKind
import com.example.cursor.diff.DiffLineRenderToken
import com.example.cursor.diff.FileDiffRenderModel
import com.example.cursor.model.CodeDiffCardState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CodeDiffCard(
  state: CodeDiffCardState,
  modifier: Modifier = Modifier,
) {
  CursorCard(modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      Text(state.title, style = MaterialTheme.typography.titleMedium)
      state.files.forEach { file -> FileDiff(file) }
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        state.touchedFiles.take(3).forEach { file ->
          CursorChip("${file.name} / ${file.path}", Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun FileDiff(file: FileDiffRenderModel) {
  Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    Text(
      text = "${file.filePath}  +${file.addedLineCount} -${file.removedLineCount}",
      color = CursorColors.Muted,
      style = MaterialTheme.typography.labelLarge,
    )
    file.hunks.forEach { hunk ->
      Column(
        Modifier
          .fillMaxWidth()
          .background(CursorColors.Soft.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
          .padding(CursorSpacing.Md),
      ) {
        Text(hunk.header, color = CursorColors.Muted, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(CursorSpacing.Xs))
        hunk.lines.forEach { line -> DiffLine(line) }
      }
    }
  }
}

@Composable
private fun DiffLine(line: DiffLineRenderToken) {
  val background =
    when (line.kind) {
      DiffLineKind.Added -> CursorColors.CodeGreen
      DiffLineKind.Removed -> CursorColors.CodeRed
      DiffLineKind.Context -> CursorColors.Soft.copy(alpha = 0f)
    }
  val prefix =
    when (line.kind) {
      DiffLineKind.Added -> "+"
      DiffLineKind.Removed -> "-"
      DiffLineKind.Context -> " "
    }
  Box(Modifier.fillMaxWidth().background(background, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
    Text(
      text = "${line.oldLineNumber ?: ""} ${line.newLineNumber ?: ""} $prefix ${line.text}",
      fontFamily = FontFamily.Monospace,
      fontWeight = if (line.kind == DiffLineKind.Context) FontWeight.Normal else FontWeight.Medium,
      style = MaterialTheme.typography.labelSmall,
    )
  }
}
