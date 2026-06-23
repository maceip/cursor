package com.example.cursor.nav

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
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
) =
  entryProvider<NavKey> {
    entry<ConversationKey>(
      metadata = { key -> CursorSceneMetadata.conversation(key.threadId) },
    ) {
      ConversationPane(
        conversation = conversation,
        onWorkbenchSelected = onWorkbenchSelected,
      )
    }
    entry<WorkbenchKey>(
      metadata = { key -> CursorSceneMetadata.workbench(key.threadId) },
    ) { key ->
      PhoneConversationLayout(
        conversation = conversation,
        workbench = workbench.takeIf { it.kind == key.kind } ?: workbench,
        topology = topology,
        onWorkbenchSelected = onWorkbenchSelected,
      )
    }
  }
