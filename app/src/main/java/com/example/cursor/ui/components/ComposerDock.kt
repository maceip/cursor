package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ComposerState
import com.example.cursor.model.PromptTokenKind
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorElevation
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ComposerDock(
  state: ComposerState,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = CursorColors.Card,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = CursorElevation.Dock,
  ) {
    Column(Modifier.padding(CursorSpacing.Lg), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      if (state.attachments.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
          state.attachments.take(2).forEach { attachment ->
            CursorChip("${attachment.label}  ${attachment.detail}", Modifier.weight(1f))
          }
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        state.promptTokens.forEach { token ->
          CursorChip(
            text = token.value,
            selected = token.kind != PromptTokenKind.PlainText,
          )
        }
      }
      Text(state.placeholder, color = CursorColors.Muted, style = MaterialTheme.typography.bodyLarge)
      if (state.autocompleteSuggestions.isNotEmpty()) {
        CursorCard(Modifier.fillMaxWidth()) {
          Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
            state.autocompleteSuggestions.forEach { suggestion ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(suggestion.label, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(suggestion.detail, color = CursorColors.Muted, style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        CursorChip("+")
        CursorChip("Attach")
        Spacer(Modifier.weight(1f))
        CursorChip(if (state.isVoiceReady) "Voice ready" else "Voice off")
        FloatingActionButton(
          onClick = {},
          modifier = Modifier.size(48.dp),
          containerColor = CursorColors.Ink,
          contentColor = Color.White,
        ) {
          Text("Send", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}
