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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.cursor.model.SpecCardState
import com.example.cursor.model.TaskItem
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun SpecCards(
  state: SpecCardState,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    CursorCard(Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        Text(state.title, style = MaterialTheme.typography.titleMedium)
        state.bullets.forEachIndexed { index, bullet ->
          Row {
            Text("${index + 1}.", Modifier.width(CursorSpacing.Xl), color = CursorColors.Muted)
            Column {
              Text(bullet.label, fontWeight = FontWeight.SemiBold)
              Text(bullet.body, color = CursorColors.Ink.copy(alpha = 0.82f))
            }
          }
        }
      }
    }
    CursorCard(Modifier.fillMaxWidth()) {
      Column {
        Text("Next steps", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(CursorSpacing.Sm))
        state.nextSteps.forEach { task -> TaskLine(task) }
      }
    }
  }
}

@Composable
fun TaskLine(task: TaskItem) {
  Row(Modifier.fillMaxWidth()) {
    Text(if (task.complete) "[x]" else "[ ]", color = CursorColors.Muted)
    Spacer(Modifier.width(CursorSpacing.Sm))
    Column {
      Text(task.label)
      task.detail?.let { Text(it, color = CursorColors.Muted, style = MaterialTheme.typography.labelMedium) }
    }
  }
}
