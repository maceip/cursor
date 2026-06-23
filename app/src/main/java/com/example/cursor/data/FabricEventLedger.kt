package com.example.cursor.data

import com.example.cursor.model.FabricPacket
import kotlinx.coroutines.flow.Flow

interface FabricEventLedger {
  fun packets(): Flow<List<FabricPacket>>
  fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>>
  suspend fun currentPackets(): List<FabricPacket>
  suspend fun latestSequenceNumber(): Long
  suspend fun append(packet: FabricPacket)
  suspend fun appendAll(packets: List<FabricPacket>)
}
