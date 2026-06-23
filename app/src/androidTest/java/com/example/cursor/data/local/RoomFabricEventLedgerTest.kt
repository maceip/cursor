package com.example.cursor.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.cursor.data.RoomFabricEventLedger
import com.example.cursor.data.local.database.CursorDatabase
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.DiffDelta
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class RoomFabricEventLedgerTest {
  private var database: CursorDatabase? = null

  @After
  fun tearDown() {
    database?.close()
  }

  @Test
  fun appendsAndReplaysPacketsBySequence() = runTest {
    val database =
      Room.inMemoryDatabaseBuilder(
          ApplicationProvider.getApplicationContext(),
          CursorDatabase::class.java,
        )
        .build()
        .also { this@RoomFabricEventLedgerTest.database = it }
    val ledger = RoomFabricEventLedger(database)
    val statusPacket =
      FabricPacket(
        packetId = "packet-1",
        sequenceNumber = 1,
        timestampMs = 1_000,
        hostId = "host-a",
        workspaceId = "workspace-a",
        agentRunId = "run-a",
        payload = FabricPayload.StatusChanged(AgentStatus.ExecutingTool, "gradle test"),
      )
    val diffPacket =
      FabricPacket(
        packetId = "packet-2",
        sequenceNumber = 2,
        timestampMs = 2_000,
        hostId = "host-a",
        workspaceId = "workspace-a",
        agentRunId = "run-a",
        payload = FabricPayload.DiffChanged(DiffDelta("Main.kt", 3, listOf("@@ hunk @@", "+ next"))),
      )

    ledger.appendAll(listOf(diffPacket, statusPacket))

    assertEquals(2L, ledger.latestSequenceNumber())
    assertEquals(listOf(statusPacket, diffPacket), ledger.currentPackets())
    assertEquals(listOf(diffPacket), ledger.packetsAfter(1).first())
  }
}
