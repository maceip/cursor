package com.example.cursor.model

data class FabricUpstreamSignal(
  val signalId: String,
  val timestampMs: Long,
  val payload: FabricUpstreamPayload,
)

sealed interface FabricUpstreamPayload {
  data class ActionApproval(
    val interactionId: String,
    val approved: Boolean,
    val messageOverride: String?,
  ) : FabricUpstreamPayload

  data class UserMessage(
    val text: String,
  ) : FabricUpstreamPayload

  data class CancelTask(
    val agentRunId: String,
  ) : FabricUpstreamPayload
}
