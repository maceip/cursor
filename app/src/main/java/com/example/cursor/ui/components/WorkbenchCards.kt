package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.WorkbenchState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun WorkbenchCards(
  state: WorkbenchState,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    CursorCard(Modifier.fillMaxWidth()) {
      Column {
        Text(state.title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(CursorSpacing.Xs))
        Text(state.summary, color = CursorColors.Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(CursorSpacing.Md))
        CursorChip(state.statusLabel)
      }
    }
    state.spec?.let { SpecCards(it) }
    state.diff?.let { CodeDiffCard(it) }
    state.handoff?.let { HandoffCard(it) }
    state.artifact?.let { ArtifactPreview(it) }
    state.draft?.let { DraftCard(it) }
  }
}
