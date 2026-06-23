package com.example.cursor.data.fabric

import com.example.cursor.data.fabric.FabricPayload.DiffDelta
import com.example.cursor.data.fabric.FabricPayload.Status
import com.example.cursor.data.fabric.FabricPayload.TokenChunk
import com.example.cursor.diff.DiffDeltaParser

object FabricTopologyReducer {
  fun reduce(
    topology: Map<String, HostTopology>,
    packet: FabricPacket,
  ): Map<String, HostTopology> {
    val host =
      topology[packet.hostId] ?: HostTopology(hostId = packet.hostId, workspaceId = packet.workspaceId)
    val agent = host.activeAgents[packet.agentRunId] ?: AgentRunState(agentRunId = packet.agentRunId)
    val updatedAgent =
      when (val payload = packet.payload) {
        is Status ->
          agent.copy(
            status = payload.state,
            activeTool = payload.activeTool,
            latestSequenceNumber = packet.sequenceNumber,
          )
        is TokenChunk ->
          agent.copy(
            transcript = agent.transcript + payload.textDelta,
            latestSequenceNumber = packet.sequenceNumber,
          )
        is DiffDelta -> {
          val renderModel = DiffDeltaParser.parse(payload.filePath, payload.lineStart, payload.hunks)
          agent.copy(
            modifiedFiles = agent.modifiedFiles + (payload.filePath to renderModel),
            latestSequenceNumber = packet.sequenceNumber,
          )
        }
      }

    return topology + (packet.hostId to host.copy(activeAgents = host.activeAgents + (packet.agentRunId to updatedAgent)))
  }
}
