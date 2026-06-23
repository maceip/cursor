package com.example.cursor.data

import androidx.room.withTransaction
import com.example.cursor.data.local.database.CursorDatabase
import com.example.cursor.data.local.entity.toDiffHunkEntities
import com.example.cursor.data.local.entity.toEntity
import com.example.cursor.data.local.relation.toModel
import com.example.cursor.model.FabricPacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFabricEventLedger(
  private val database: CursorDatabase,
) : FabricEventLedger {
  private val dao = database.fabricPacketDao()

  override fun packets(): Flow<List<FabricPacket>> = dao.observePackets().map { records -> records.map { it.toModel() } }

  override fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>> =
    dao.observePacketsAfter(sequenceNumber).map { records -> records.map { it.toModel() } }

  override suspend fun currentPackets(): List<FabricPacket> = dao.getPackets().map { it.toModel() }

  override suspend fun latestSequenceNumber(): Long = dao.getLatestSequenceNumber() ?: 0L

  override suspend fun append(packet: FabricPacket) {
    database.withTransaction {
      dao.insertPacket(packet.toEntity())
      dao.deleteDiffHunks(packet.packetId)
      dao.insertDiffHunks(packet.toDiffHunkEntities())
    }
  }

  override suspend fun appendAll(packets: List<FabricPacket>) {
    database.withTransaction {
      packets.forEach { packet ->
        dao.insertPacket(packet.toEntity())
        dao.deleteDiffHunks(packet.packetId)
        dao.insertDiffHunks(packet.toDiffHunkEntities())
      }
    }
  }
}
