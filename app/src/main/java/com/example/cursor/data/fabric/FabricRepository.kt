package com.example.cursor.data.fabric

import com.example.cursor.model.CursorWorkspaceState
import kotlinx.coroutines.flow.StateFlow

interface FabricRepository {
  val topology: StateFlow<Map<String, HostTopology>>
  val workspaceState: StateFlow<CursorWorkspaceState>

  fun connect(lastKnownSequence: Long = 0)

  fun disconnect()
}
