package com.example.cursor.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.ChecklistItem
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.SourceCard
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.motion.cursorBackMorph
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun WorkbenchCard(
  title: String,
  modifier: Modifier = Modifier,
  eyebrow: String? = null,
  icon: ImageVector? = null,
  trailing: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .cursorBackMorph(scaleXTarget = 0.96f, scaleYTarget = 0.9f, alphaTarget = 0.78f, translateY = 14f),
    shape = CursorShape.Card,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = 0.dp,
  ) {
    Column(Modifier.padding(horizontal = CursorSpacing.Sm, vertical = CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
          Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = CursorColors.Ink)
          Spacer(Modifier.width(CursorSpacing.Sm))
        }
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
  icon: ImageVector? = null,
  contentPadding: PaddingValues = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
) {
  Surface(
    modifier = modifier,
    shape = CursorShape.Chip,
    color = if (selected) CursorColors.BlueSoft else CursorColors.Surface,
    border = BorderStroke(1.dp, if (selected) CursorColors.Blue.copy(alpha = 0.35f) else CursorColors.Stroke),
  ) {
    Row(
      modifier = Modifier.padding(contentPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Xs),
    ) {
      if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = if (selected) CursorColors.Blue else CursorColors.Ink)
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) CursorColors.Blue else CursorColors.Ink,
      )
    }
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
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs)) {
    items.forEach { item ->
      Row(verticalAlignment = Alignment.Top) {
        Box(
          modifier =
            Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(if (item.completed) CursorColors.Ink else CursorColors.Surface)
              .border(1.dp, CursorColors.Stroke, CircleShape)
        ) {
          Icon(
            imageVector = if (item.completed) Icons.Outlined.Check else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(if (item.completed) 10.dp else 12.dp),
            tint = if (item.completed) Color.White else CursorColors.Muted,
          )
        }
        Spacer(Modifier.width(CursorSpacing.Sm))
        Text(item.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = CursorColors.Ink)
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

@Composable
fun CursorIconButton(
  icon: ImageVector,
  modifier: Modifier = Modifier,
  dark: Boolean = false,
  onClick: () -> Unit = {},
) {
  Surface(
    modifier = modifier.size(34.dp),
    shape = CircleShape,
    color = if (dark) CursorColors.Ink else CursorColors.SurfaceSoft,
    border = if (dark) null else BorderStroke(1.dp, CursorColors.Stroke),
    onClick = onClick,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (dark) Color.White else CursorColors.Ink)
    }
  }
}

@Composable
fun SourceCardView(source: SourceCard, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.cursorBackMorph(scaleXTarget = 0.98f, scaleYTarget = 0.92f, alphaTarget = 0.8f, translateY = 8f),
    shape = CursorShape.CardSmall,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
    ) {
      Box(
        modifier =
          Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(CursorColors.SurfaceSoft),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp), tint = CursorColors.Ink)
      }
      Column(Modifier.weight(1f)) {
        Text(source.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(source.detail, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
      }
      Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = CursorColors.Ink)
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
