package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cursor.model.HandoffWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun HandoffCard(
  handoff: HandoffWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(title = "Sent to Cursor Desktop", eyebrow = "Handoff") {
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Md), verticalAlignment = Alignment.CenterVertically) {
        Text("Mobile", style = MaterialTheme.typography.titleMedium)
        Text("to", color = CursorColors.Rust)
        Text(handoff.targetDevice, style = MaterialTheme.typography.titleMedium)
      }
      Text("Continue the active thread on desktop with attached workspace context.", color = CursorColors.Muted)
    }
    WorkbenchCard(title = "Handoff checklist") { Checklist(handoff.items) }
  }
}
