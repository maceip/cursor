package com.example.cursor.data

import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.flow.StateFlow

interface FabricRepository {
  val conversation: StateFlow<ConversationState>
  val activeWorkbench: StateFlow<WorkbenchState>
  val topology: StateFlow<FabricTopologyState>

  fun openWorkbench(kind: WorkbenchKind)
}
