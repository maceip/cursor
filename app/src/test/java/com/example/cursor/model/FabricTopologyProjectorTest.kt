package com.example.cursor.model

import junit.framework.TestCase.assertEquals
import org.junit.Test

class FabricTopologyProjectorTest {
  @Test
  fun fromPackets_projectsRunsInSequenceOrder() {
    val packets =
      listOf(
        FabricPacket(
          packetId = "p3",
          sequenceNumber = 3,
          timestampMs = 3_000,
          hostId = "host-a",
          workspaceId = "workspace-a",
          agentRunId = "run-a",
          payload = FabricPayload.DiffChanged(DiffDelta("Main.kt", 12, listOf("+ hello"))),
        ),
        FabricPacket(
          packetId = "p1",
          sequenceNumber = 1,
          timestampMs = 1_000,
          hostId = "host-a",
          workspaceId = "workspace-a",
          agentRunId = "run-a",
          payload = FabricPayload.StatusChanged(AgentStatus.ExecutingTool, "gradle test"),
        ),
        FabricPacket(
          packetId = "p2",
          sequenceNumber = 2,
          timestampMs = 2_000,
          hostId = "host-a",
          workspaceId = "workspace-a",
          agentRunId = "run-a",
          payload = FabricPayload.TokenChunk("Compiling"),
        ),
      )

    val topology = FabricTopologyProjector.fromPackets(packets)
    val run = topology.hosts.single().agentRuns.single()

    assertEquals(3L, topology.latestSequenceNumber)
    assertEquals(AgentStatus.ExecutingTool, run.status)
    assertEquals("gradle test", run.activeTool)
    assertEquals("Compiling", run.tokenPreview)
    assertEquals(listOf("Main.kt"), run.modifiedFiles)
  }
}
