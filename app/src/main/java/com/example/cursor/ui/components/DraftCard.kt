package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.cursor.model.DraftCardState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun DraftCard(
  state: DraftCardState,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      state.tones.forEach { tone ->
        CursorChip(
          text = tone,
          selected = tone == state.selectedTone,
          modifier = Modifier.weight(1f),
        )
      }
    }
    CursorCard(Modifier.fillMaxWidth()) {
      Column {
        Text("Refined draft: ${state.selectedTone}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(CursorSpacing.Md))
        Text("Subject", color = CursorColors.Muted, style = MaterialTheme.typography.labelLarge)
        Text(state.subject, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(CursorSpacing.Md))
        Text("Body", color = CursorColors.Muted, style = MaterialTheme.typography.labelLarge)
        Text(state.body, style = MaterialTheme.typography.bodyMedium)
      }
    }
    CursorCard(Modifier.fillMaxWidth()) {
      Column {
        Text("Why this works", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(CursorSpacing.Sm))
        Text(state.rationale, color = CursorColors.Ink)
      }
    }
  }
}
