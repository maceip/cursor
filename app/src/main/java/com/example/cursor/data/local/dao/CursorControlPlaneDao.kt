package com.example.cursor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cursor.data.local.entity.CursorAccountEntity
import com.example.cursor.data.local.entity.CursorAgentEntity
import com.example.cursor.data.local.entity.CursorArtifactEntity
import com.example.cursor.data.local.entity.CursorModelEntity
import com.example.cursor.data.local.entity.CursorPendingRequestEntity
import com.example.cursor.data.local.entity.CursorRepositoryEntity
import com.example.cursor.data.local.entity.CursorRunEntity
import com.example.cursor.data.local.entity.CursorSyncCursorEntity
import com.example.cursor.data.local.entity.CursorUsageEntity
import com.example.cursor.data.local.entity.CursorWorkerEntity
import com.example.cursor.data.local.entity.CursorWorkerSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CursorControlPlaneDao {
  @Query("SELECT * FROM cursor_accounts ORDER BY accountType ASC")
  fun observeAccounts(): Flow<List<CursorAccountEntity>>

  @Query("SELECT * FROM cursor_models ORDER BY isDefault DESC, displayName ASC")
  fun observeModels(): Flow<List<CursorModelEntity>>

  @Query("SELECT * FROM cursor_repositories ORDER BY owner ASC, name ASC")
  fun observeRepositories(): Flow<List<CursorRepositoryEntity>>

  @Query("SELECT * FROM cursor_agents ORDER BY updatedAt DESC, createdAt DESC")
  fun observeAgents(): Flow<List<CursorAgentEntity>>

  @Query("SELECT * FROM cursor_runs ORDER BY updatedAt DESC, createdAt DESC")
  fun observeRuns(): Flow<List<CursorRunEntity>>

  @Query("SELECT * FROM cursor_worker_summary WHERE id = :id")
  fun observeWorkerSummary(id: String = CursorWorkerSummaryEntity.SingletonId): Flow<CursorWorkerSummaryEntity?>

  @Query("SELECT * FROM cursor_workers ORDER BY status ASC, id ASC")
  fun observeWorkers(): Flow<List<CursorWorkerEntity>>

  @Query("SELECT * FROM cursor_pending_requests ORDER BY createdAtMs DESC")
  fun observePendingRequests(): Flow<List<CursorPendingRequestEntity>>

  @Query("SELECT * FROM cursor_artifacts ORDER BY updatedAt DESC, path ASC")
  fun observeArtifacts(): Flow<List<CursorArtifactEntity>>

  @Query("SELECT * FROM cursor_usage ORDER BY lastSyncedAtMs DESC")
  fun observeUsage(): Flow<List<CursorUsageEntity>>

  @Query("SELECT * FROM cursor_accounts WHERE accountType = :accountType")
  suspend fun account(accountType: String): CursorAccountEntity?

  @Query("SELECT * FROM cursor_agents ORDER BY updatedAt DESC, createdAt DESC LIMIT 1")
  suspend fun latestAgent(): CursorAgentEntity?

  @Query("SELECT * FROM cursor_runs WHERE agentId = :agentId ORDER BY updatedAt DESC, createdAt DESC LIMIT 1")
  suspend fun latestRunForAgent(agentId: String): CursorRunEntity?

  @Query("SELECT * FROM cursor_runs WHERE id = :runId")
  suspend fun run(runId: String): CursorRunEntity?

  @Query("SELECT * FROM cursor_runs WHERE agentId = :agentId ORDER BY updatedAt DESC, createdAt DESC")
  suspend fun runsForAgent(agentId: String): List<CursorRunEntity>

  @Query("SELECT * FROM cursor_sync_cursors WHERE key = :key")
  suspend fun syncCursor(key: String): CursorSyncCursorEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAccount(account: CursorAccountEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertModels(models: List<CursorModelEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertRepositories(repositories: List<CursorRepositoryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAgents(agents: List<CursorAgentEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertRuns(runs: List<CursorRunEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWorkerSummary(summary: CursorWorkerSummaryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWorkers(workers: List<CursorWorkerEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPendingRequests(requests: List<CursorPendingRequestEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertArtifacts(artifacts: List<CursorArtifactEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertUsage(usage: List<CursorUsageEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSyncCursor(cursor: CursorSyncCursorEntity)

  @Query("DELETE FROM cursor_accounts WHERE accountType = :accountType")
  suspend fun deleteAccount(accountType: String)

  @Query("DELETE FROM cursor_models")
  suspend fun clearModels()

  @Query("DELETE FROM cursor_repositories")
  suspend fun clearRepositories()

  @Query("DELETE FROM cursor_agents")
  suspend fun clearAgents()

  @Query("DELETE FROM cursor_runs")
  suspend fun clearRuns()

  @Query("DELETE FROM cursor_runs WHERE agentId = :agentId")
  suspend fun clearRunsForAgent(agentId: String)

  @Query("DELETE FROM cursor_runs WHERE agentId NOT IN (:agentIds)")
  suspend fun clearRunsForStaleAgents(agentIds: List<String>)

  @Query("DELETE FROM cursor_worker_summary")
  suspend fun clearWorkerSummary()

  @Query("DELETE FROM cursor_workers")
  suspend fun clearWorkers()

  @Query("DELETE FROM cursor_pending_requests")
  suspend fun clearPendingRequests()

  @Query("DELETE FROM cursor_artifacts")
  suspend fun clearArtifacts()

  @Query("DELETE FROM cursor_artifacts WHERE agentId = :agentId")
  suspend fun clearArtifactsForAgent(agentId: String)

  @Query("DELETE FROM cursor_artifacts WHERE agentId NOT IN (:agentIds)")
  suspend fun clearArtifactsForStaleAgents(agentIds: List<String>)

  @Query("DELETE FROM cursor_usage")
  suspend fun clearUsage()

  @Query("DELETE FROM cursor_usage WHERE agentId = :agentId")
  suspend fun clearUsageForAgent(agentId: String)

  @Query("DELETE FROM cursor_usage WHERE agentId NOT IN (:agentIds)")
  suspend fun clearUsageForStaleAgents(agentIds: List<String>)

  @Query("DELETE FROM cursor_sync_cursors")
  suspend fun clearSyncCursors()

  @Transaction
  suspend fun replaceModels(models: List<CursorModelEntity>) {
    clearModels()
    upsertModels(models)
  }

  @Transaction
  suspend fun replaceRepositories(repositories: List<CursorRepositoryEntity>) {
    clearRepositories()
    upsertRepositories(repositories)
  }

  @Transaction
  suspend fun replaceAgents(agents: List<CursorAgentEntity>) {
    clearAgents()
    upsertAgents(agents)
    val agentIds = agents.map { it.id }
    if (agentIds.isEmpty()) {
      clearRuns()
      clearArtifacts()
      clearUsage()
    } else {
      clearRunsForStaleAgents(agentIds)
      clearArtifactsForStaleAgents(agentIds)
      clearUsageForStaleAgents(agentIds)
    }
  }

  @Transaction
  suspend fun replaceRunsForAgent(agentId: String, runs: List<CursorRunEntity>) {
    clearRunsForAgent(agentId)
    upsertRuns(runs)
  }

  @Transaction
  suspend fun replaceArtifactsForAgent(agentId: String, artifacts: List<CursorArtifactEntity>) {
    clearArtifactsForAgent(agentId)
    upsertArtifacts(artifacts)
  }

  @Transaction
  suspend fun replaceUsageForAgent(agentId: String, usage: List<CursorUsageEntity>) {
    clearUsageForAgent(agentId)
    upsertUsage(usage)
  }

  @Transaction
  suspend fun replacePoolPlane(
    summary: CursorWorkerSummaryEntity,
    workers: List<CursorWorkerEntity>,
    pendingRequests: List<CursorPendingRequestEntity>,
  ) {
    clearWorkerSummary()
    clearWorkers()
    clearPendingRequests()
    upsertWorkerSummary(summary)
    upsertWorkers(workers)
    upsertPendingRequests(pendingRequests)
  }

  @Transaction
  suspend fun clearRemoteCache() {
    clearModels()
    clearRepositories()
    clearAgents()
    clearRuns()
    clearWorkerSummary()
    clearWorkers()
    clearPendingRequests()
    clearArtifacts()
    clearUsage()
    clearSyncCursors()
  }
}
