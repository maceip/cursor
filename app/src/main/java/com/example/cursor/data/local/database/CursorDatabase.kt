package com.example.cursor.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cursor.data.local.dao.CursorControlPlaneDao
import com.example.cursor.data.local.dao.FabricPacketDao
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
import com.example.cursor.data.local.entity.FabricDiffHunkEntity
import com.example.cursor.data.local.entity.FabricPacketEntity

@Database(
  entities =
    [
      FabricPacketEntity::class,
      FabricDiffHunkEntity::class,
      CursorAccountEntity::class,
      CursorModelEntity::class,
      CursorRepositoryEntity::class,
      CursorAgentEntity::class,
      CursorRunEntity::class,
      CursorWorkerSummaryEntity::class,
      CursorWorkerEntity::class,
      CursorPendingRequestEntity::class,
      CursorArtifactEntity::class,
      CursorUsageEntity::class,
      CursorSyncCursorEntity::class,
    ],
  version = 3,
  exportSchema = true,
)
abstract class CursorDatabase : RoomDatabase() {
  abstract fun fabricPacketDao(): FabricPacketDao
  abstract fun cursorControlPlaneDao(): CursorControlPlaneDao

  companion object {
    @Volatile private var instance: CursorDatabase? = null

    private val Migration1To2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_accounts` (
              `accountType` TEXT NOT NULL,
              `apiKeyName` TEXT NOT NULL,
              `principal` TEXT NOT NULL,
              `isServiceAccount` INTEGER NOT NULL,
              `verifiedAtMs` INTEGER NOT NULL,
              `linked` INTEGER NOT NULL,
              PRIMARY KEY(`accountType`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_models` (
              `id` TEXT NOT NULL,
              `displayName` TEXT NOT NULL,
              `description` TEXT NOT NULL,
              `isDefault` INTEGER NOT NULL,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_repositories` (
              `url` TEXT NOT NULL,
              `owner` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`url`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_agents` (
              `id` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `url` TEXT NOT NULL,
              `latestRunId` TEXT,
              `repositoryUrl` TEXT,
              `targetKind` TEXT,
              `targetName` TEXT,
              `createdAt` TEXT,
              `updatedAt` TEXT,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_runs` (
              `id` TEXT NOT NULL,
              `agentId` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `result` TEXT,
              `createdAt` TEXT,
              `updatedAt` TEXT,
              `lastEventId` TEXT,
              `streamRetentionSeconds` INTEGER,
              `terminal` INTEGER NOT NULL,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_worker_summary` (
              `id` TEXT NOT NULL,
              `connectedCount` INTEGER NOT NULL,
              `inUseCount` INTEGER NOT NULL,
              `idleCount` INTEGER NOT NULL,
              `pendingCount` INTEGER NOT NULL,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_workers` (
              `id` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `poolName` TEXT,
              `machineName` TEXT,
              `repositoryUrl` TEXT,
              `labels` TEXT NOT NULL,
              `lastSeenAt` TEXT,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_pending_requests` (
              `id` TEXT NOT NULL,
              `repositoryUrl` TEXT,
              `poolName` TEXT,
              `labels` TEXT NOT NULL,
              `createdAtMs` INTEGER,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_artifacts` (
              `agentId` TEXT NOT NULL,
              `path` TEXT NOT NULL,
              `sizeBytes` INTEGER,
              `updatedAt` TEXT,
              `downloadUrl` TEXT,
              `downloadExpiresAt` TEXT,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`agentId`, `path`)
            )
            """.trimIndent(),
          )
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_sync_cursors` (
              `key` TEXT NOT NULL,
              `value` TEXT NOT NULL,
              `updatedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`key`)
            )
            """.trimIndent(),
          )
        }
      }

    private val Migration2To3 =
      object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cursor_usage` (
              `agentId` TEXT NOT NULL,
              `runId` TEXT NOT NULL,
              `inputTokens` INTEGER NOT NULL,
              `outputTokens` INTEGER NOT NULL,
              `cacheReadTokens` INTEGER NOT NULL,
              `cacheWriteTokens` INTEGER NOT NULL,
              `totalTokens` INTEGER NOT NULL,
              `lastSyncedAtMs` INTEGER NOT NULL,
              PRIMARY KEY(`agentId`, `runId`)
            )
            """.trimIndent(),
          )
        }
      }

    fun getInstance(context: Context): CursorDatabase =
      instance ?: synchronized(this) {
        instance
          ?: Room.databaseBuilder(
              context.applicationContext,
              CursorDatabase::class.java,
              "cursor.db",
            )
            .addMigrations(Migration1To2, Migration2To3)
            .build()
            .also { instance = it }
      }
  }
}
