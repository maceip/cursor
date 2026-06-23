package com.example.cursor.data.fabric

import com.example.cursor.data.fabric.FabricPayload.DiffDelta
import com.example.cursor.data.fabric.FabricPayload.Status
import com.example.cursor.data.fabric.FabricPayload.TokenChunk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FabricTopologyReducerTest {
  @Test
  fun reduce_appliesPacketsBySequenceToAgentState() {
    val statusPacket = packet(1, Status(AgentRunStatus.ExecutingTool, "bash"))
    val tokenPacket = packet(2, TokenChunk("Running tests."))
    val diffPacket =
      packet(
        3,
        DiffDelta(
          filePath = "src/Main.kt",
          lineStart = 10,
          hunks = listOf("@@ -10,1 +10,2 @@", " fun main() {", "+  println(\"Cursor\")"),
        ),
      )

    val topology =
      listOf(statusPacket, tokenPacket, diffPacket).fold(emptyMap<String, HostTopology>(), FabricTopologyReducer::reduce)
    val agent = topology.getValue("host-1").activeAgents.getValue("agent-1")

    assertEquals(AgentRunStatus.ExecutingTool, agent.status)
    assertEquals("bash", agent.activeTool)
    assertEquals("Running tests.", agent.transcript)
    assertEquals(3, agent.latestSequenceNumber)
    assertEquals(1, agent.modifiedFiles.getValue("src/Main.kt").addedLineCount)
  }

  private fun packet(sequenceNumber: Long, payload: FabricPayload) =
    FabricPacket(
      packetId = "packet-$sequenceNumber",
      sequenceNumber = sequenceNumber,
      timestampMs = sequenceNumber,
      hostId = "host-1",
      workspaceId = "workspace-1",
      agentRunId = "agent-1",
      payload = payload,
    )
}
