package com.example.cursor.data.cursor

import com.example.cursor.data.FabricStreamClient
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload
import com.example.cursor.model.FabricUpstreamPayload
import com.example.cursor.model.FabricUpstreamSignal
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

@OptIn(ExperimentalCoroutinesApi::class)
class CursorFabricBridgeClient(
  private val controlPlane: CursorControlPlaneRepository,
  private val authStore: CursorAuthStore,
  private val apiClient: CursorApiClient,
) : FabricStreamClient {
  override fun packetsAfter(lastKnownSequence: Long): Flow<FabricPacket> {
    val sequence = AtomicLong(lastKnownSequence)
    return controlPlane.streamTarget
      .flatMapLatest { target ->
        if (target == null) {
          emptyFlow()
        } else {
          val token = authStore.token(CursorAccountKind.User) ?: return@flatMapLatest emptyFlow()
          apiClient
            .streamRun(token, target)
            .catch { failure ->
              if ((failure as? CursorApiException)?.statusCode == 410) {
                controlPlane.refreshRunAfterStreamExpired(target)
                controlPlane.recordStreamEvent(
                  target,
                  CursorStreamEvent(
                    id = null,
                    type = "error",
                    data = JSONObject().put("message", "Stream expired; refreshed run state from Cursor.").toString(),
                    retentionSeconds = null,
                  ),
                )
              }
            }
            .flatMapLatest { event ->
              flow {
                controlPlane.recordStreamEvent(target, event)
                event.toFabricPackets(target, sequence).forEach { packet -> emit(packet) }
              }
            }
        }
      }
  }

  override suspend fun send(signal: FabricUpstreamSignal) {
    when (val payload = signal.payload) {
      is FabricUpstreamPayload.UserMessage -> controlPlane.createRunFromComposer(payload.text)
      is FabricUpstreamPayload.CancelTask -> controlPlane.cancelRun(payload.agentRunId)
      is FabricUpstreamPayload.ActionApproval -> Unit
    }
  }

  private fun CursorStreamEvent.toFabricPackets(
    target: CursorRunStreamTarget,
    sequence: AtomicLong,
  ): List<FabricPacket> {
    val json = runCatching { if (data.isBlank()) JSONObject() else JSONObject(data) }.getOrDefault(JSONObject())
    val payloads =
      when (type) {
        "assistant", "thinking" ->
          listOfNotNull(json.optStringOrNull("text")?.let { FabricPayload.TokenChunk(it) })
        "tool_call" ->
          listOf(FabricPayload.StatusChanged(AgentStatus.ExecutingTool, json.optStringOrNull("name") ?: "tool"))
        "status" ->
          listOf(FabricPayload.StatusChanged(json.optString("status").toAgentStatus(), null))
        "interaction_update" -> interactionPayloads(json)
        "result" ->
          listOfNotNull(
            json.optStringOrNull("text")?.let { FabricPayload.TokenChunk(it) },
            FabricPayload.StatusChanged(json.optString("status").toAgentStatus(), null),
          )
        "error" ->
          listOf(FabricPayload.StatusChanged(AgentStatus.Idle, json.optStringOrNull("message") ?: "stream error"))
        else -> emptyList()
      }

    return payloads.map { payload ->
      val nextSequence = sequence.incrementAndGet()
      FabricPacket(
        packetId = "cursor-${target.runId}-${id ?: type}-$nextSequence",
        sequenceNumber = nextSequence,
        timestampMs = System.currentTimeMillis(),
        hostId = "cursor-api",
        workspaceId = target.agentId,
        agentRunId = target.runId,
        payload = payload,
      )
    }
  }

  private fun interactionPayloads(json: JSONObject): List<FabricPayload> {
    val subtype = json.optString("type")
    val text = json.optStringOrNull("text") ?: json.optStringOrNull("delta")
    return when {
      text != null -> listOf(FabricPayload.TokenChunk(text))
      subtype.contains("tool", ignoreCase = true) ->
        listOf(FabricPayload.StatusChanged(AgentStatus.ExecutingTool, json.optStringOrNull("name") ?: subtype))
      subtype.contains("turn-ended", ignoreCase = true) -> listOf(FabricPayload.StatusChanged(AgentStatus.Idle, null))
      else -> emptyList()
    }
  }

  private fun String.toAgentStatus(): AgentStatus =
    when (uppercase()) {
      "CREATING", "QUEUED", "RUNNING" -> AgentStatus.Thinking
      "WAITING_FOR_USER", "AWAITING_APPROVAL" -> AgentStatus.AwaitingApproval
      "FINISHED", "CANCELLED", "ERROR", "EXPIRED" -> AgentStatus.Idle
      else -> AgentStatus.Thinking
    }

  private fun JSONObject.optStringOrNull(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null
}
