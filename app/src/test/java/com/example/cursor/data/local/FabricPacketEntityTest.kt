package com.example.cursor.data.local

import com.example.cursor.data.local.entity.toDiffHunkEntities
import com.example.cursor.data.local.entity.toEntity
import com.example.cursor.data.local.relation.FabricPacketRecord
import com.example.cursor.data.local.relation.toModel
import com.example.cursor.model.DiffDelta
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FabricPacketEntityTest {
  @Test
  fun diffPayload_roundTripsThroughRoomRecordShape() {
    val packet =
      FabricPacket(
        packetId = "packet-diff",
        sequenceNumber = 9,
        timestampMs = 9_000,
        hostId = "host-a",
        workspaceId = "workspace-a",
        agentRunId = "run-a",
        payload =
          FabricPayload.DiffChanged(
            DiffDelta(
              filePath = "app/src/main/java/Main.kt",
              lineStart = 7,
              hunks = listOf("@@ header @@", "- old", "+ new"),
            )
          ),
      )

    val record =
      FabricPacketRecord(
        packet = packet.toEntity(),
        diffHunks = packet.toDiffHunkEntities().reversed(),
      )

    assertEquals(packet, record.toModel())
  }
}
