package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.SpecWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecCards(
  spec: SpecWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(title = "Spec overview", icon = Icons.AutoMirrored.Outlined.FormatListBulleted) {
      spec.sections.forEachIndexed { index, section ->
        Row {
          Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
          Spacer(Modifier.width(18.dp))
          Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs)) {
            Text(section.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(section.body, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
          }
        }
      }
    }
    WorkbenchCard(title = "Next steps", icon = Icons.Outlined.CheckBox) { Checklist(spec.nextSteps) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      spec.sources.forEach { source ->
        SourceCardView(source, modifier = Modifier.weight(1f))
      }
    }
  }
}
