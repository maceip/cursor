package com.example.cursor.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cursor.data.FakeFabricRepository
import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import com.example.cursor.ui.components.ArtifactPreview
import com.example.cursor.ui.components.ChatPane
import com.example.cursor.ui.components.CodeDiffCard
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.components.CursorHeader
import com.example.cursor.ui.components.DraftCard
import com.example.cursor.ui.components.HandoffCard
import com.example.cursor.ui.components.SpecCards
import com.example.cursor.ui.components.StatusPill
import com.example.cursor.ui.components.TopologyStrip
import com.example.cursor.ui.components.WorkbenchCard
import com.example.cursor.ui.components.WorkbenchKindTabs
import com.example.cursor.ui.theme.CursorClaudeTheme
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun PhoneConversationLayout(
  conversation: ConversationState,
  workbench: WorkbenchState,
  topology: FabricTopologyState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
      .padding(horizontal = 18.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    CursorHeader(conversation)
    ChatPane(
      conversation = conversation,
      onWorkbenchSelected = onWorkbenchSelected,
      modifier = Modifier.weight(1f),
      inlineWorkbench = { WorkbenchBody(workbench, topology, onWorkbenchSelected) },
    )
    ComposerDock(conversation.composer)
  }
}

@Composable
fun ConversationPane(
  conversation: ConversationState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
      .padding(CursorSpacing.Xl),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    CursorHeader(conversation)
    ChatPane(
      conversation = conversation,
      onWorkbenchSelected = onWorkbenchSelected,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
fun WorkbenchPane(
  workbench: WorkbenchState,
  topology: FabricTopologyState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier =
      modifier
        .fillMaxHeight()
        .background(CursorColors.Cream),
    contentPadding = PaddingValues(CursorSpacing.Xl),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item {
      WorkbenchCard(
        title = workbench.title,
        eyebrow = workbench.status,
        trailing = { StatusPill(workbench.kind.name) },
      ) {
        androidx.compose.material3.Text(workbench.summary)
        WorkbenchKindTabs(
          kinds = WorkbenchKind.entries,
          selectedKind = workbench.kind,
          onKindSelected = onWorkbenchSelected,
        )
      }
    }
    item { WorkbenchBody(workbench, topology, onWorkbenchSelected) }
  }
}

@Composable
fun WorkbenchBody(
  workbench: WorkbenchState,
  topology: FabricTopologyState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    when (workbench.kind) {
      WorkbenchKind.Spec -> workbench.spec?.let { SpecCards(it) }
      WorkbenchKind.CodeReview -> workbench.codeReview?.let { CodeDiffCard(it) }
      WorkbenchKind.Handoff -> workbench.handoff?.let { HandoffCard(it) }
      WorkbenchKind.Artifact -> workbench.artifact?.let { ArtifactPreview(it) }
      WorkbenchKind.Writing -> workbench.draft?.let { DraftCard(it) }
    }
    TopologyStrip(topology)
    WorkbenchKindTabs(
      kinds = WorkbenchKind.entries,
      selectedKind = workbench.kind,
      onKindSelected = onWorkbenchSelected,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
fun CursorTwoPaneLayout(
  conversation: @Composable () -> Unit,
  workbench: @Composable () -> Unit,
  composer: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
  ) {
    Row(Modifier.weight(1f).fillMaxWidth()) {
      Column(Modifier.weight(0.38f).fillMaxHeight()) { conversation() }
      Spacer(
        Modifier
          .fillMaxHeight()
          .width(1.dp)
          .padding(vertical = CursorSpacing.Xl)
          .background(CursorColors.Stroke)
      )
      Column(Modifier.weight(0.62f).fillMaxHeight()) { workbench() }
    }
    composer()
  }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
private fun PhoneConversationLayoutPreview() {
  val repository = FakeFabricRepository()
  CursorClaudeTheme {
    PhoneConversationLayout(
      conversation = repository.conversation.value,
      workbench = repository.activeWorkbench.value,
      topology = repository.topology.value,
      onWorkbenchSelected = {},
    )
  }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun CursorTwoPaneLayoutPreview() {
  val repository = FakeFabricRepository()
  CursorClaudeTheme {
    CursorTwoPaneLayout(
      conversation = { ConversationPane(repository.conversation.value, onWorkbenchSelected = {}) },
      workbench = {
        WorkbenchPane(
          workbench = repository.activeWorkbench.value,
          topology = repository.topology.value,
          onWorkbenchSelected = {},
        )
      },
      composer = {
        ComposerDock(
          composer = repository.conversation.value.composer,
          modifier = Modifier.padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
        )
      },
    )
  }
}
