package com.example.cursor.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cursor.data.local.entity.FabricDiffHunkEntity
import com.example.cursor.data.local.entity.FabricPacketEntity
import com.example.cursor.data.local.entity.PayloadTypeDiffChanged
import com.example.cursor.data.local.entity.PayloadTypeStatusChanged
import com.example.cursor.data.local.entity.PayloadTypeTokenChunk
import com.example.cursor.data.local.entity.status
import com.example.cursor.model.DiffDelta
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload

data class FabricPacketRecord(
  @Embedded val packet: FabricPacketEntity,
  @Relation(parentColumn = "packetId", entityColumn = "packetId") val diffHunks: List<FabricDiffHunkEntity>,
)

internal fun FabricPacketRecord.toModel(): FabricPacket =
  FabricPacket(
    packetId = packet.packetId,
    sequenceNumber = packet.sequenceNumber,
    timestampMs = packet.timestampMs,
    hostId = packet.hostId,
    workspaceId = packet.workspaceId,
    agentRunId = packet.agentRunId,
    payload = payload(),
  )

private fun FabricPacketRecord.payload(): FabricPayload =
  when (packet.payloadType) {
    PayloadTypeStatusChanged -> FabricPayload.StatusChanged(packet.status(), packet.activeTool)
    PayloadTypeTokenChunk -> FabricPayload.TokenChunk(requireNotNull(packet.textDelta) { "Token payload ${packet.packetId} is missing text." })
    PayloadTypeDiffChanged ->
      FabricPayload.DiffChanged(
        DiffDelta(
          filePath = requireNotNull(packet.diffFilePath) { "Diff payload ${packet.packetId} is missing file path." },
          lineStart = requireNotNull(packet.diffLineStart) { "Diff payload ${packet.packetId} is missing line start." },
          hunks = diffHunks.sortedBy { it.lineIndex }.map { it.line },
        )
      )

    else -> error("Unsupported fabric payload type ${packet.payloadType}.")
  }
