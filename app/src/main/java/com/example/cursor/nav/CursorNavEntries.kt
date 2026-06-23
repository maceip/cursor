package com.example.cursor.nav

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import com.example.cursor.ui.shell.ConversationPane
import com.example.cursor.ui.shell.PhoneConversationLayout

fun cursorEntryProvider(
  conversation: ConversationState,
  workbench: WorkbenchState,
  topology: FabricTopologyState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
): (NavKey) -> NavEntry<NavKey> =
  { key: NavKey ->
    when (key) {
      is ConversationKey ->
        NavEntry<NavKey>(
          key = key,
          metadata = CursorSceneMetadata.conversation(key.threadId),
        ) {
      ConversationPane(
        conversation = conversation,
        onWorkbenchSelected = onWorkbenchSelected,
      )
        }
      is WorkbenchKey ->
        NavEntry<NavKey>(
          key = key,
          metadata = CursorSceneMetadata.workbench(key.threadId),
        ) {
      PhoneConversationLayout(
        conversation = conversation,
        workbench = workbench.takeIf { it.kind == key.kind } ?: workbench,
        topology = topology,
        onWorkbenchSelected = onWorkbenchSelected,
      )
        }
      else -> error("Unknown Cursor nav key: $key")
    }
  }
