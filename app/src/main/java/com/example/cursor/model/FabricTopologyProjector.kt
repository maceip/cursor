package com.example.cursor.model

object FabricTopologyProjector {
  fun fromPackets(packets: List<FabricPacket>): FabricTopologyState {
    val sortedPackets = packets.sortedBy { it.sequenceNumber }
    val hosts = linkedMapOf<HostKey, MutableHostTopology>()

    sortedPackets.forEach { packet ->
      val hostKey = HostKey(packet.hostId, packet.workspaceId)
      val host = hosts.getOrPut(hostKey) { MutableHostTopology(hostKey) }
      val run = host.run(packet.agentRunId)

      when (val payload = packet.payload) {
        is FabricPayload.StatusChanged -> {
          run.status = payload.status
          run.activeTool = payload.activeTool
        }

        is FabricPayload.TokenChunk -> run.tokenPreview.append(payload.textDelta)

        is FabricPayload.DiffChanged -> {
          val filePath = payload.delta.filePath
          if (filePath !in run.modifiedFiles) {
            run.modifiedFiles += filePath
          }
        }
      }
    }

    return FabricTopologyState(
      latestSequenceNumber = sortedPackets.lastOrNull()?.sequenceNumber ?: 0L,
      hosts = hosts.values.map { it.toModel() },
    )
  }
}

private data class HostKey(
  val hostId: String,
  val workspaceId: String,
)

private class MutableHostTopology(
  private val key: HostKey,
) {
  private val runs = linkedMapOf<String, MutableAgentRunState>()

  fun run(agentRunId: String): MutableAgentRunState = runs.getOrPut(agentRunId) { MutableAgentRunState(agentRunId) }

  fun toModel(): HostTopology =
    HostTopology(
      hostId = key.hostId,
      workspaceId = key.workspaceId,
      agentRuns = runs.values.map { it.toModel() },
    )
}

private class MutableAgentRunState(
  private val agentRunId: String,
) {
  var status: AgentStatus = AgentStatus.Idle
  var activeTool: String? = null
  val tokenPreview = StringBuilder()
  val modifiedFiles = mutableListOf<String>()

  fun toModel(): AgentRunState =
    AgentRunState(
      agentRunId = agentRunId,
      status = status,
      activeTool = activeTool,
      tokenPreview = tokenPreview.toString(),
      modifiedFiles = modifiedFiles.toList(),
    )
}
