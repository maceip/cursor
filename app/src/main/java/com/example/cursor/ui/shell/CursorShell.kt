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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cursor.data.FakeFabricRepository
import com.example.cursor.model.ConversationState
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
import com.example.cursor.ui.theme.CursorClaudeTheme
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun PhoneConversationLayout(
  conversation: ConversationState,
  workbench: WorkbenchState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md),
  ) {
    CursorHeader(conversation)
    ChatPane(
      conversation = conversation,
      onWorkbenchSelected = onWorkbenchSelected,
      modifier = Modifier.weight(1f),
      inlineWorkbench = { WorkbenchBody(workbench) },
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
      .statusBarsPadding()
      .padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md),
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
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier =
      modifier
        .fillMaxHeight()
        .background(CursorColors.Cream),
    contentPadding = PaddingValues(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md),
  ) {
    item { WorkbenchBody(workbench) }
  }
}

@Composable
fun WorkbenchBody(
  workbench: WorkbenchState,
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
private fun PhoneSpecPreview() {
  PhonePreview(WorkbenchKind.Spec)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
private fun PhoneCodeReviewPreview() {
  PhonePreview(WorkbenchKind.CodeReview)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
private fun PhoneHandoffPreview() {
  PhonePreview(WorkbenchKind.Handoff)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
private fun PhoneArtifactPreview() {
  PhonePreview(WorkbenchKind.Artifact)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
private fun PhoneWritingPreview() {
  PhonePreview(WorkbenchKind.Writing)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun FoldSpecPreview() {
  FoldPreview(WorkbenchKind.Spec)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun FoldCodeReviewPreview() {
  FoldPreview(WorkbenchKind.CodeReview)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun FoldHandoffPreview() {
  FoldPreview(WorkbenchKind.Handoff)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun FoldArtifactPreview() {
  FoldPreview(WorkbenchKind.Artifact)
}

@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
private fun FoldWritingPreview() {
  FoldPreview(WorkbenchKind.Writing)
}

@Composable
private fun PhonePreview(kind: WorkbenchKind) {
  val repository = remember { FakeFabricRepository(initialKind = kind) }
  val conversation by repository.conversation.collectAsState()
  val workbench by repository.activeWorkbench.collectAsState()
  CursorClaudeTheme {
    PhoneConversationLayout(
      conversation = conversation,
      workbench = workbench,
      onWorkbenchSelected = {},
    )
  }
}

@Composable
private fun FoldPreview(kind: WorkbenchKind) {
  val repository = remember { FakeFabricRepository(initialKind = kind) }
  val conversation by repository.conversation.collectAsState()
  val workbench by repository.activeWorkbench.collectAsState()
  CursorClaudeTheme {
    CursorTwoPaneLayout(
      conversation = { ConversationPane(conversation, onWorkbenchSelected = {}) },
      workbench = {
        WorkbenchPane(
          workbench = workbench,
          onWorkbenchSelected = {},
        )
      },
      composer = {
        ComposerDock(
          composer = conversation.composer,
          modifier = Modifier.padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
        )
      },
    )
  }
}
