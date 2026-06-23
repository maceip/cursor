package com.example.cursor.data

import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FabricRepository {
  val conversation: StateFlow<ConversationState>
  val activeWorkbench: StateFlow<WorkbenchState>
  val topology: StateFlow<FabricTopologyState>
  val packets: StateFlow<List<FabricPacket>>

  fun connect()
  fun disconnect()
  fun openWorkbench(kind: WorkbenchKind)
  fun submitUserMessage(text: String)
  fun approveInteraction(interactionId: String, approved: Boolean, messageOverride: String?)
  fun cancelAgentRun(agentRunId: String)
  fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>>
  suspend fun appendPacket(packet: FabricPacket)
}
