package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.DraftWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DraftCard(
  draft: DraftWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      draft.tones.forEach { tone -> CursorChip(tone, selected = tone == draft.selectedTone || draft.selectedTone.startsWith(tone)) }
    }
    WorkbenchCard(title = draft.selectedTone, eyebrow = "Refined draft") {
      Text("Subject", style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
      Text(draft.subject, style = MaterialTheme.typography.titleMedium)
      Text("Body", style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
      Text(draft.body, style = MaterialTheme.typography.bodyMedium)
    }
    WorkbenchCard(title = "Why this works") {
      Text(draft.rationale, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted)
    }
  }
}
