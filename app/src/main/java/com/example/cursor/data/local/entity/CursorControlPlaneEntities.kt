package com.example.cursor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cursor_accounts")
data class CursorAccountEntity(
  @PrimaryKey val accountType: String,
  val apiKeyName: String,
  val principal: String,
  val isServiceAccount: Boolean,
  val verifiedAtMs: Long,
  val linked: Boolean,
)

@Entity(tableName = "cursor_models")
data class CursorModelEntity(
  @PrimaryKey val id: String,
  val displayName: String,
  val description: String,
  val isDefault: Boolean,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_repositories")
data class CursorRepositoryEntity(
  @PrimaryKey val url: String,
  val owner: String,
  val name: String,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_agents")
data class CursorAgentEntity(
  @PrimaryKey val id: String,
  val name: String,
  val status: String,
  val url: String,
  val latestRunId: String?,
  val repositoryUrl: String?,
  val targetKind: String?,
  val targetName: String?,
  val createdAt: String?,
  val updatedAt: String?,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_runs")
data class CursorRunEntity(
  @PrimaryKey val id: String,
  val agentId: String,
  val status: String,
  val result: String?,
  val createdAt: String?,
  val updatedAt: String?,
  val lastEventId: String?,
  val streamRetentionSeconds: Long?,
  val terminal: Boolean,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_worker_summary")
data class CursorWorkerSummaryEntity(
  @PrimaryKey val id: String = SingletonId,
  val connectedCount: Int,
  val inUseCount: Int,
  val idleCount: Int,
  val pendingCount: Int,
  val lastSyncedAtMs: Long,
) {
  companion object {
    const val SingletonId = "team"
  }
}

@Entity(tableName = "cursor_workers")
data class CursorWorkerEntity(
  @PrimaryKey val id: String,
  val status: String,
  val poolName: String?,
  val machineName: String?,
  val repositoryUrl: String?,
  val labels: String,
  val lastSeenAt: String?,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_pending_requests")
data class CursorPendingRequestEntity(
  @PrimaryKey val id: String,
  val repositoryUrl: String?,
  val poolName: String?,
  val labels: String,
  val createdAtMs: Long?,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_artifacts", primaryKeys = ["agentId", "path"])
data class CursorArtifactEntity(
  val agentId: String,
  val path: String,
  val sizeBytes: Long?,
  val updatedAt: String?,
  val downloadUrl: String?,
  val downloadExpiresAt: String?,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_usage", primaryKeys = ["agentId", "runId"])
data class CursorUsageEntity(
  val agentId: String,
  val runId: String,
  val inputTokens: Long,
  val outputTokens: Long,
  val cacheReadTokens: Long,
  val cacheWriteTokens: Long,
  val totalTokens: Long,
  val lastSyncedAtMs: Long,
)

@Entity(tableName = "cursor_sync_cursors")
data class CursorSyncCursorEntity(
  @PrimaryKey val key: String,
  val value: String,
  val updatedAtMs: Long,
)
