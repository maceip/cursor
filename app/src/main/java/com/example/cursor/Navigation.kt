package com.example.cursor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.cursor.data.fabric.FakeFabricRepository
import com.example.cursor.nav.ConversationKey
import com.example.cursor.nav.CursorFoldableSceneStrategy
import com.example.cursor.nav.WorkbenchKey
import com.example.cursor.nav.WorkbenchKind
import com.example.cursor.nav.conversationEntry
import com.example.cursor.nav.workbenchEntry
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.shell.ConversationPane
import com.example.cursor.ui.shell.PhoneConversationLayout
import com.example.cursor.ui.shell.WorkbenchPane

@Composable
fun MainNavigation() {
  val repository = remember { FakeFabricRepository() }
  LaunchedEffect(repository) { repository.connect() }
  val workspaceState by repository.workspaceState.collectAsStateWithLifecycle()
  val threadId = workspaceState.thread.threadId
  val backStack: NavBackStack<NavKey> =
    rememberNavBackStack(
      ConversationKey(threadId),
      WorkbenchKey(threadId, WorkbenchKind.Spec),
    )

  BoxWithConstraints(Modifier.fillMaxSize()) {
    NavDisplay(
      backStack = backStack,
      modifier = Modifier.fillMaxSize(),
      onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
      sceneStrategy =
        CursorFoldableSceneStrategy(
            windowWidthDp = maxWidth,
            conversation = { ConversationPane(workspaceState.thread) },
            workbench = { _, kind -> WorkbenchPane(workspaceState.workbenches.getValue(kind)) },
            composer = { ComposerDock(workspaceState.thread.composer) },
          )
          .then(SinglePaneSceneStrategy()),
      entryProvider = { key ->
        when (key) {
          is ConversationKey ->
            conversationEntry(key) {
              PhoneConversationLayout(
                thread = workspaceState.thread,
                workbench = null,
              )
            }
          is WorkbenchKey ->
            workbenchEntry(key) { workbenchKey ->
              PhoneConversationLayout(
                thread = workspaceState.thread,
                workbench = workspaceState.workbenches.getValue(workbenchKey.kind),
              )
            }
          else -> error("Unsupported Cursor nav key: $key")
        }
      },
    )
  }
}
