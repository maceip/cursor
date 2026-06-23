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
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
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
  onSubmit: (String) -> Unit = {},
) {
  var draft by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

  fun submitDraft() {
    val text = draft.text.trim()
    if (text.isEmpty()) return
    onSubmit(text)
    draft = TextFieldValue("")
  }

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
      OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(composer.promptHint, color = CursorColors.Muted) },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = false,
        maxLines = 4,
      )
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
        Button(
          onClick = ::submitDraft,
          modifier = Modifier.height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CursorColors.Ink),
        ) {
          Text("Send", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}
