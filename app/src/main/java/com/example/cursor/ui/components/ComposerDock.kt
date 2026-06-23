package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ComposerState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorElevation
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposerDock(
  composer: ComposerState,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = CursorShape.Dock,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = CursorElevation.Raised,
  ) {
    Column(Modifier.padding(CursorSpacing.Lg), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        composer.attachments.forEach { attachment -> CursorChip("${attachment.label} - ${attachment.detail}") }
      }
      Text(composer.promptHint, style = MaterialTheme.typography.bodyLarge, color = CursorColors.Muted)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        composer.tokens.forEach { token -> CursorChip(token.value, selected = true) }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        composer.quickActions.forEach { action ->
          CursorChip(action)
          Spacer(Modifier.size(CursorSpacing.Sm))
        }
        Spacer(Modifier.weight(1f))
        if (composer.isVoiceReady) CursorChip("Mic ready")
        Spacer(Modifier.size(CursorSpacing.Sm))
        FloatingActionButton(
          onClick = {},
          modifier = Modifier.size(48.dp),
          containerColor = CursorColors.Ink,
          contentColor = Color.White,
        ) {
          Text("Go", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}
