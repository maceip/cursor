package com.example.cursor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload

@Entity(
  tableName = "fabric_diff_hunks",
  primaryKeys = ["packetId", "lineIndex"],
  foreignKeys =
    [
      ForeignKey(
        entity = FabricPacketEntity::class,
        parentColumns = ["packetId"],
        childColumns = ["packetId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("packetId")],
)
data class FabricDiffHunkEntity(
  val packetId: String,
  val lineIndex: Int,
  val line: String,
)

internal fun FabricPacket.toDiffHunkEntities(): List<FabricDiffHunkEntity> =
  when (val packetPayload = payload) {
    is FabricPayload.DiffChanged ->
      packetPayload.delta.hunks.mapIndexed { index, line ->
        FabricDiffHunkEntity(
          packetId = packetId,
          lineIndex = index,
          line = line,
        )
      }

    else -> emptyList()
  }
