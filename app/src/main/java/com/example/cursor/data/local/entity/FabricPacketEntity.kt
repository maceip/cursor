package com.example.cursor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload

@Entity(
  tableName = "fabric_packets",
  indices =
    [
      Index(value = ["sequenceNumber"], unique = true),
      Index(value = ["hostId", "workspaceId", "agentRunId"]),
    ],
)
data class FabricPacketEntity(
  @PrimaryKey val packetId: String,
  val sequenceNumber: Long,
  val timestampMs: Long,
  val hostId: String,
  val workspaceId: String,
  val agentRunId: String,
  val payloadType: String,
  val status: String?,
  val activeTool: String?,
  val textDelta: String?,
  val diffFilePath: String?,
  val diffLineStart: Int?,
)

internal const val PayloadTypeStatusChanged = "status_changed"
internal const val PayloadTypeTokenChunk = "token_chunk"
internal const val PayloadTypeDiffChanged = "diff_changed"

internal fun FabricPacket.toEntity(): FabricPacketEntity =
  when (val packetPayload = payload) {
    is FabricPayload.StatusChanged ->
      FabricPacketEntity(
        packetId = packetId,
        sequenceNumber = sequenceNumber,
        timestampMs = timestampMs,
        hostId = hostId,
        workspaceId = workspaceId,
        agentRunId = agentRunId,
        payloadType = PayloadTypeStatusChanged,
        status = packetPayload.status.name,
        activeTool = packetPayload.activeTool,
        textDelta = null,
        diffFilePath = null,
        diffLineStart = null,
      )

    is FabricPayload.TokenChunk ->
      FabricPacketEntity(
        packetId = packetId,
        sequenceNumber = sequenceNumber,
        timestampMs = timestampMs,
        hostId = hostId,
        workspaceId = workspaceId,
        agentRunId = agentRunId,
        payloadType = PayloadTypeTokenChunk,
        status = null,
        activeTool = null,
        textDelta = packetPayload.textDelta,
        diffFilePath = null,
        diffLineStart = null,
      )

    is FabricPayload.DiffChanged ->
      FabricPacketEntity(
        packetId = packetId,
        sequenceNumber = sequenceNumber,
        timestampMs = timestampMs,
        hostId = hostId,
        workspaceId = workspaceId,
        agentRunId = agentRunId,
        payloadType = PayloadTypeDiffChanged,
        status = null,
        activeTool = null,
        textDelta = null,
        diffFilePath = packetPayload.delta.filePath,
        diffLineStart = packetPayload.delta.lineStart,
      )
  }

internal fun FabricPacketEntity.status(): AgentStatus =
  AgentStatus.valueOf(requireNotNull(status) { "Status payload $packetId is missing status." })
