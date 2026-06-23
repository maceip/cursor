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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.cursor.model.HandoffCardState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun HandoffCard(
  state: HandoffCardState,
  modifier: Modifier = Modifier,
) {
  CursorCard(modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      Text("Sent to Cursor Desktop", style = MaterialTheme.typography.titleMedium)
      Text(state.message, color = CursorColors.Muted)
      Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Phone", color = CursorColors.Muted, fontWeight = FontWeight.SemiBold)
        Text("->", color = CursorColors.Rust, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(state.targetDevice, color = CursorColors.Ink, fontWeight = FontWeight.SemiBold)
      }
      CursorChip("Continue on desktop")
      Spacer(Modifier.height(CursorSpacing.Xs))
      state.tasks.forEach { task -> TaskLine(task) }
    }
  }
}
