package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.SpecWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun SpecCards(
  spec: SpecWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(title = "Spec overview", eyebrow = "Product skeleton") {
      spec.sections.forEachIndexed { index, section ->
        Text("${index + 1}. ${section.title}", style = MaterialTheme.typography.titleMedium)
        Text(section.body, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted)
      }
    }
    WorkbenchCard(title = "Next steps", eyebrow = "Implementation") { Checklist(spec.nextSteps) }
  }
}
