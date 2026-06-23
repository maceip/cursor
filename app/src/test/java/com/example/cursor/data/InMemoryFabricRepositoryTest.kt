package com.example.cursor.data

import com.example.cursor.model.AgentStatus
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload
import com.example.cursor.model.WorkbenchKind
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InMemoryFabricRepositoryTest {
  @Test
  fun startsWithProductionShapedConversationAndTopology() {
    val repository = InMemoryFabricRepository()

    assertEquals(FabricDefaults.DefaultThreadId, repository.conversation.value.threadId)
    assertEquals(WorkbenchKind.Spec, repository.activeWorkbench.value.kind)
    assertTrue(repository.packets.value.isNotEmpty())
    assertTrue(repository.topology.value.hosts.isNotEmpty())
    assertEquals(repository.packets.value.maxOf { it.sequenceNumber }, repository.topology.value.latestSequenceNumber)
  }

  @Test
  fun openWorkbench_switchesActiveWorkbenchShape() {
    val repository = InMemoryFabricRepository()

    repository.openWorkbench(WorkbenchKind.CodeReview)

    assertEquals(WorkbenchKind.CodeReview, repository.activeWorkbench.value.kind)
    assertTrue(repository.activeWorkbench.value.codeReview?.diff?.files?.isNotEmpty() == true)
  }

  @Test
  fun appendPacket_updatesPacketLedgerAndTopology() = runTest {
    val repository = InMemoryFabricRepository()
    val packet =
      FabricPacket(
        packetId = "packet-99-review",
        sequenceNumber = 99,
        timestampMs = 99_000,
        hostId = "cursor-cloud-worker",
        workspaceId = "shape-prototype",
        agentRunId = "review-run",
        payload = FabricPayload.StatusChanged(AgentStatus.AwaitingApproval, "apply_patch"),
      )

    repository.appendPacket(packet)

    assertEquals(listOf(packet), repository.packetsAfter(98).first())
    assertEquals(99L, repository.topology.value.latestSequenceNumber)
    assertTrue(repository.topology.value.hosts.any { host -> host.agentRuns.any { it.agentRunId == "review-run" } })
  }
}
