package com.example.cursor.data

import com.example.cursor.model.FabricPacket
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomFabricRepositoryTest {
  @Test
  fun startsWithWarmTopologyBeforeRoomEmits() = runTest {
    val repository =
      RoomFabricRepository(
        ledger = MemoryLedger(),
        scope = backgroundScope,
      )

    assertEquals(42L, repository.topology.value.latestSequenceNumber)
    assertTrue(repository.topology.value.hosts.isNotEmpty())
  }

  private class MemoryLedger : FabricEventLedger {
    private val packets = MutableStateFlow<List<FabricPacket>>(emptyList())

    override fun packets(): Flow<List<FabricPacket>> = packets

    override fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>> =
      packets.map { list -> list.filter { it.sequenceNumber > sequenceNumber } }

    override suspend fun currentPackets(): List<FabricPacket> = packets.value

    override suspend fun latestSequenceNumber(): Long = packets.value.maxOfOrNull { it.sequenceNumber } ?: 0L

    override suspend fun append(packet: FabricPacket) {
      packets.value = (packets.value.filterNot { it.packetId == packet.packetId } + packet).sortedBy { it.sequenceNumber }
    }

    override suspend fun appendAll(packets: List<FabricPacket>) {
      packets.forEach { append(it) }
    }
  }
}
