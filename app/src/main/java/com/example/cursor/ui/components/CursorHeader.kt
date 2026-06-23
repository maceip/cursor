package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.CursorThreadState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CursorHeader(
  thread: CursorThreadState,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      CursorLogo()
      Spacer(Modifier.width(CursorSpacing.Sm))
      Text(
        text = "CURSOR",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
      )
      Spacer(Modifier.weight(1f))
      Text("History", color = CursorColors.Muted, style = MaterialTheme.typography.labelLarge)
      Spacer(Modifier.width(CursorSpacing.Lg))
      Text("More", color = CursorColors.Muted, style = MaterialTheme.typography.labelLarge)
    }
    Spacer(Modifier.height(CursorSpacing.Lg))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
    ) {
      CursorChip(thread.workspaceName, Modifier.weight(1f))
      CursorChip(thread.modelName, Modifier.weight(1f))
    }
  }
}
