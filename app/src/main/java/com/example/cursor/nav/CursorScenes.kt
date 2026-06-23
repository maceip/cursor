package com.example.cursor.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.shell.CursorTwoPaneLayout

class CursorFoldableSceneStrategy(
  private val canShowTwoPane: Boolean,
  private val conversationContent: @Composable (ConversationKey) -> Unit,
  private val workbenchContent: @Composable (WorkbenchKey) -> Unit,
  private val composerContent: @Composable () -> Unit,
) : SceneStrategy<NavKey> {
  override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
    if (!canShowTwoPane) return null

    val workbenchEntry = entries.lastOrNull()?.takeIf { CursorSceneMetadata.paneRole(it.metadata) == PaneRole.Workbench } ?: return null
    val workbenchKey = workbenchEntry.contentKey as? WorkbenchKey ?: return null
    val conversationEntry =
      entries
        .dropLast(1)
        .findLast {
          CursorSceneMetadata.paneRole(it.metadata) == PaneRole.Conversation &&
            CursorSceneMetadata.threadId(it.metadata) == workbenchKey.threadId
        } ?: return null
    val conversationKey = conversationEntry.contentKey as? ConversationKey ?: return null

    return CursorTwoPaneScene(
      key = "cursor-two-pane-${workbenchKey.threadId}-${workbenchKey.kind.name}",
      previousEntries = entries.dropLast(1),
      conversationEntry = conversationEntry,
      workbenchEntry = workbenchEntry,
      conversationKey = conversationKey,
      workbenchKey = workbenchKey,
      conversationContent = conversationContent,
      workbenchContent = workbenchContent,
      composerContent = composerContent,
    )
  }
}

class CursorTwoPaneScene(
  override val key: Any,
  override val previousEntries: List<NavEntry<NavKey>>,
  private val conversationEntry: NavEntry<NavKey>,
  private val workbenchEntry: NavEntry<NavKey>,
  private val conversationKey: ConversationKey,
  private val workbenchKey: WorkbenchKey,
  private val conversationContent: @Composable (ConversationKey) -> Unit,
  private val workbenchContent: @Composable (WorkbenchKey) -> Unit,
  private val composerContent: @Composable () -> Unit,
) : Scene<NavKey> {
  override val entries: List<NavEntry<NavKey>> = listOf(conversationEntry, workbenchEntry)

  override val content: @Composable () -> Unit = {
    CursorTwoPaneLayout(
      conversation = { conversationContent(conversationKey) },
      workbench = { workbenchContent(workbenchKey) },
      composer = composerContent,
    )
  }
}

fun compatiblePanePair(
  previousRole: PaneRole?,
  previousThreadId: String?,
  latestRole: PaneRole?,
  latestThreadId: String?,
  latestKind: WorkbenchKind?,
): Boolean =
  previousRole == PaneRole.Conversation &&
    latestRole == PaneRole.Workbench &&
    previousThreadId != null &&
    previousThreadId == latestThreadId &&
    latestKind != null
