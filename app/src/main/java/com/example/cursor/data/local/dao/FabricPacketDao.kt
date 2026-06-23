package com.example.cursor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cursor.data.local.entity.FabricDiffHunkEntity
import com.example.cursor.data.local.entity.FabricPacketEntity
import com.example.cursor.data.local.relation.FabricPacketRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FabricPacketDao {
  @Transaction
  @Query("SELECT * FROM fabric_packets ORDER BY sequenceNumber ASC")
  fun observePackets(): Flow<List<FabricPacketRecord>>

  @Transaction
  @Query("SELECT * FROM fabric_packets WHERE sequenceNumber > :sequenceNumber ORDER BY sequenceNumber ASC")
  fun observePacketsAfter(sequenceNumber: Long): Flow<List<FabricPacketRecord>>

  @Transaction
  @Query("SELECT * FROM fabric_packets ORDER BY sequenceNumber ASC")
  suspend fun getPackets(): List<FabricPacketRecord>

  @Query("SELECT MAX(sequenceNumber) FROM fabric_packets")
  suspend fun getLatestSequenceNumber(): Long?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPacket(packet: FabricPacketEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDiffHunks(diffHunks: List<FabricDiffHunkEntity>)

  @Query("DELETE FROM fabric_diff_hunks WHERE packetId = :packetId")
  suspend fun deleteDiffHunks(packetId: String)
}
