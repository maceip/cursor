package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
      draft.tones.forEachIndexed { index, tone ->
        CursorChip(
          tone,
          selected = tone == draft.selectedTone,
          icon =
            when (index) {
              0 -> Icons.Outlined.AutoAwesome
              1 -> Icons.Outlined.Email
              else -> Icons.Outlined.Person
            },
        )
      }
    }
    WorkbenchCard(
      title = "Refined draft: ${draft.selectedTone}",
      icon = Icons.Outlined.Email,
      trailing = { Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = CursorColors.Ink) },
    ) {
      FieldLabel("Subject")
      Text(draft.subject, style = MaterialTheme.typography.bodyLarge)
      FieldLabel("Body")
      Text(draft.body, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
    }
    WorkbenchCard(title = "Why this works", icon = Icons.Outlined.WorkOutline) {
      Text(draft.rationale, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
    }
  }
}

@Composable
private fun FieldLabel(text: String) {
  Text(text, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted, fontWeight = FontWeight.Medium)
}
