package com.example.cursor.data

import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricUpstreamSignal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface FabricStreamClient {
  fun packetsAfter(lastKnownSequence: Long): Flow<FabricPacket>
  suspend fun send(signal: FabricUpstreamSignal)
}

object OfflineFabricStreamClient : FabricStreamClient {
  override fun packetsAfter(lastKnownSequence: Long): Flow<FabricPacket> = emptyFlow()

  override suspend fun send(signal: FabricUpstreamSignal) = Unit
}
