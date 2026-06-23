package com.example.cursor.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.example.cursor.data.fabric.FakeFabricFixtures
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

class CursorFoldableSceneStrategy(
  private val windowWidthDp: Dp,
  private val conversation: @Composable (String) -> Unit,
  private val workbench: @Composable (String, WorkbenchKind) -> Unit,
  private val composer: @Composable () -> Unit,
) : SceneStrategy<NavKey> {
  override fun SceneStrategyScope<NavKey>.calculateScene(
    entries: List<NavEntry<NavKey>>,
  ): Scene<NavKey>? {
    if (windowWidthDp < FoldableWidthThreshold) return null
    val pair = findCompatiblePair(entries) ?: return null
    val conversationEntry = entries[pair.conversationEntryIndex]
    val workbenchEntry = entries[pair.workbenchEntryIndex]
    return CursorTwoPaneScene(
      key = "cursor-two-pane-${pair.threadId}-${pair.workbenchKind}",
      entries = listOf(conversationEntry, workbenchEntry),
      previousEntries = entries.dropLast(2),
      threadId = pair.threadId,
      workbenchKind = pair.workbenchKind,
      conversation = conversation,
      workbench = workbench,
      composer = composer,
    )
  }
}

private val FoldableWidthThreshold = 600.dp

fun findCompatiblePair(entries: List<NavEntry<NavKey>>): CursorPanePair? {
  if (entries.size < 2) return null
  val lastIndex = entries.lastIndex
  val previousIndex = lastIndex - 1
  val previous = entries[previousIndex]
  val last = entries[lastIndex]
  val previousRole = previous.metadata[CursorSceneMetadata.PaneRole]
  val lastRole = last.metadata[CursorSceneMetadata.PaneRole]
  val previousThread = previous.metadata[CursorSceneMetadata.ThreadId] as? String
  val lastThread = last.metadata[CursorSceneMetadata.ThreadId] as? String
  if (previousThread == null || previousThread != lastThread) return null

  return when {
    previousRole == PaneRole.Conversation && lastRole == PaneRole.Workbench ->
      CursorPanePair(
        threadId = previousThread,
        conversationEntryIndex = previousIndex,
        workbenchEntryIndex = lastIndex,
        workbenchKind = last.metadata[CursorSceneMetadata.WorkbenchKind] as WorkbenchKind,
      )
    previousRole == PaneRole.Workbench && lastRole == PaneRole.Conversation ->
      CursorPanePair(
        threadId = previousThread,
        conversationEntryIndex = lastIndex,
        workbenchEntryIndex = previousIndex,
        workbenchKind = previous.metadata[CursorSceneMetadata.WorkbenchKind] as WorkbenchKind,
      )
    else -> null
  }
}

private data class CursorTwoPaneScene(
  override val key: Any,
  override val entries: List<NavEntry<NavKey>>,
  override val previousEntries: List<NavEntry<NavKey>>,
  val threadId: String,
  val workbenchKind: WorkbenchKind,
  val conversation: @Composable (String) -> Unit,
  val workbench: @Composable (String, WorkbenchKind) -> Unit,
  val composer: @Composable () -> Unit,
) : Scene<NavKey> {
  override val content: @Composable () -> Unit = {
    CursorTwoPaneSceneContent(
      conversation = { conversation(threadId) },
      workbench = { workbench(threadId, workbenchKind) },
      composer = composer,
    )
  }
}

@Composable
fun CursorTwoPaneSceneContent(
  conversation: @Composable () -> Unit,
  workbench: @Composable () -> Unit,
  composer: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxSize().background(CursorColors.Cream)) {
    Row(Modifier.weight(1f).fillMaxWidth()) {
      Box(Modifier.weight(0.38f).fillMaxHeight().padding(start = CursorSpacing.Xl, top = CursorSpacing.Xl, end = CursorSpacing.Md)) {
        conversation()
      }
      Box(Modifier.width(1.dp).fillMaxHeight().background(CursorColors.Stroke))
      Box(Modifier.weight(0.62f).fillMaxHeight().padding(start = CursorSpacing.Md, top = CursorSpacing.Xl, end = CursorSpacing.Xl)) {
        workbench()
      }
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg)) {
      composer()
    }
  }
}

@Composable
fun CursorTwoPanePreviewScene(
  conversation: @Composable () -> Unit,
  workbench: @Composable () -> Unit,
) {
  val state = remember { FakeFabricFixtures.workspaceState() }
  CursorTwoPaneSceneContent(
    conversation = conversation,
    workbench = workbench,
    composer = { ComposerDock(state.thread.composer) },
  )
}
