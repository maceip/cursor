package com.example.cursor.model

data class FabricTopologyState(
  val hosts: List<HostTopology>,
  val latestSequenceNumber: Long,
)

data class HostTopology(
  val hostId: String,
  val workspaceId: String,
  val agentRuns: List<AgentRunState>,
)

data class AgentRunState(
  val agentRunId: String,
  val status: AgentStatus,
  val activeTool: String?,
  val tokenPreview: String,
  val modifiedFiles: List<String>,
)

enum class AgentStatus {
  Idle,
  Thinking,
  ExecutingTool,
  AwaitingApproval,
}

data class FabricPacket(
  val packetId: String,
  val sequenceNumber: Long,
  val timestampMs: Long,
  val hostId: String,
  val workspaceId: String,
  val agentRunId: String,
  val payload: FabricPayload,
)

sealed interface FabricPayload {
  data class StatusChanged(val status: AgentStatus, val activeTool: String?) : FabricPayload

  data class TokenChunk(val textDelta: String) : FabricPayload

  data class DiffChanged(val delta: DiffDelta) : FabricPayload
}
