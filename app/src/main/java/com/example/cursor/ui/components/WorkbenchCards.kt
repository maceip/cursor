package com.example.cursor.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ChecklistItem
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun WorkbenchCard(
  title: String,
  modifier: Modifier = Modifier,
  eyebrow: String? = null,
  trailing: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = CursorShape.Card,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = 1.dp,
  ) {
    Column(Modifier.padding(CursorSpacing.Lg), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          if (eyebrow != null) {
            Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
          }
          Text(title, style = MaterialTheme.typography.titleMedium)
        }
        trailing?.invoke()
      }
      content()
    }
  }
}

@Composable
fun CursorChip(
  text: String,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
) {
  Surface(
    modifier = modifier,
    shape = CursorShape.Chip,
    color = if (selected) Color(0xFFEAF3FF) else CursorColors.Surface,
    border = BorderStroke(1.dp, if (selected) CursorColors.Blue.copy(alpha = 0.35f) else CursorColors.Stroke),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(contentPadding),
      style = MaterialTheme.typography.labelMedium,
      color = if (selected) CursorColors.Blue else CursorColors.Ink,
    )
  }
}

@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) {
  CursorChip(
    text = text,
    modifier = modifier,
    selected = false,
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
  )
}

@Composable
fun Checklist(items: List<ChecklistItem>, modifier: Modifier = Modifier) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    items.forEach { item ->
      Row(verticalAlignment = Alignment.Top) {
        Text(
          text = if (item.completed) "OK" else "--",
          modifier =
            Modifier
              .clip(CircleShape)
              .background(if (item.completed) CursorColors.Green else CursorColors.SurfaceSoft)
              .border(1.dp, CursorColors.Stroke, CircleShape)
              .padding(horizontal = 7.dp, vertical = 3.dp),
          style = MaterialTheme.typography.labelMedium,
          color = CursorColors.Ink,
        )
        Spacer(Modifier.width(CursorSpacing.Sm))
        Text(item.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
fun WorkbenchKindTabs(
  kinds: List<WorkbenchKind>,
  selectedKind: WorkbenchKind,
  onKindSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier, horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    kinds.forEach { kind ->
      Surface(
        onClick = { onKindSelected(kind) },
        shape = CursorShape.Chip,
        color = if (kind == selectedKind) CursorColors.Ink else CursorColors.Surface,
        border = BorderStroke(1.dp, CursorColors.Stroke),
      ) {
        Text(
          text = kind.label,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          style = MaterialTheme.typography.labelMedium,
          color = if (kind == selectedKind) Color.White else CursorColors.Ink,
        )
      }
    }
  }
}

@Composable
fun TopologyStrip(topology: FabricTopologyState, modifier: Modifier = Modifier) {
  val view = LocalView.current
  val awaitingApprovalRuns =
    topology.hosts.flatMap { host ->
      host.agentRuns.filter { it.status == AgentStatus.AwaitingApproval }.map { run -> "${host.hostId}:${run.agentRunId}" }
    }

  LaunchedEffect(awaitingApprovalRuns) {
    if (awaitingApprovalRuns.isNotEmpty()) {
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
  }

  WorkbenchCard(
    title = "Fabric topology",
    eyebrow = "seq ${topology.latestSequenceNumber}",
    modifier = modifier,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      topology.hosts.forEach { host ->
        Text(host.hostId, style = MaterialTheme.typography.labelMedium)
        host.agentRuns.forEach { run ->
          Text(
            text = "${run.agentRunId}: ${run.status.name} ${run.activeTool.orEmpty()}",
            style = MaterialTheme.typography.bodyMedium,
            color = CursorColors.Muted,
          )
        }
      }
    }
  }
}

val WorkbenchKind.label: String
  get() =
    when (this) {
      WorkbenchKind.Spec -> "Spec"
      WorkbenchKind.CodeReview -> "Code"
      WorkbenchKind.Handoff -> "Handoff"
      WorkbenchKind.Artifact -> "Artifact"
      WorkbenchKind.Writing -> "Writing"
    }
