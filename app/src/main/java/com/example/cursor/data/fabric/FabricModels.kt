package com.example.cursor.data.fabric

import com.example.cursor.diff.FileDiffRenderModel

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
  data class Status(val state: AgentRunStatus, val activeTool: String = "") : FabricPayload

  data class TokenChunk(val textDelta: String) : FabricPayload

  data class DiffDelta(
    val filePath: String,
    val lineStart: Int,
    val hunks: List<String>,
  ) : FabricPayload
}

enum class AgentRunStatus {
  Idle,
  Thinking,
  ExecutingTool,
  AwaitingApproval,
}

data class HostTopology(
  val hostId: String,
  val workspaceId: String,
  val activeAgents: Map<String, AgentRunState> = emptyMap(),
)

data class AgentRunState(
  val agentRunId: String,
  val status: AgentRunStatus = AgentRunStatus.Idle,
  val activeTool: String = "",
  val transcript: String = "",
  val modifiedFiles: Map<String, FileDiffRenderModel> = emptyMap(),
  val latestSequenceNumber: Long = 0,
)
