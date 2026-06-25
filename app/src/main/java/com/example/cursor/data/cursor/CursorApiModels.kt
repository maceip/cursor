package com.example.cursor.data.cursor

import com.example.cursor.data.local.entity.CursorAccountEntity
import com.example.cursor.data.local.entity.CursorAgentEntity
import com.example.cursor.data.local.entity.CursorArtifactEntity
import com.example.cursor.data.local.entity.CursorModelEntity
import com.example.cursor.data.local.entity.CursorPendingRequestEntity
import com.example.cursor.data.local.entity.CursorRepositoryEntity
import com.example.cursor.data.local.entity.CursorRunEntity
import com.example.cursor.data.local.entity.CursorUsageEntity
import com.example.cursor.data.local.entity.CursorWorkerEntity
import com.example.cursor.data.local.entity.CursorWorkerSummaryEntity

enum class CursorAccountKind(val storageKey: String, val label: String) {
  User("user", "User"),
  ServiceAccount("service_account", "Service account"),
  Web("web", "Cursor web"),
}

data class CursorKeyInfo(
  val apiKeyName: String,
  val createdAt: String?,
  val userId: Long?,
  val userEmail: String?,
  val userFirstName: String?,
  val userLastName: String?,
) {
  val isServiceAccount: Boolean = userId == null && userEmail == null
  val principal: String =
    userEmail
      ?: listOfNotNull(userFirstName, userLastName).joinToString(" ").takeIf { it.isNotBlank() }
      ?: apiKeyName
}

data class CursorRemoteModel(
  val id: String,
  val displayName: String,
  val description: String,
  val isDefault: Boolean,
)

data class CursorRemoteRepository(
  val url: String,
)

data class CursorRemoteAgent(
  val id: String,
  val name: String,
  val status: String,
  val url: String,
  val latestRunId: String?,
  val repositoryUrl: String?,
  val targetKind: String?,
  val targetName: String?,
  val createdAt: String?,
  val updatedAt: String?,
)

data class CursorRemoteRun(
  val id: String,
  val agentId: String,
  val status: String,
  val result: String?,
  val createdAt: String?,
  val updatedAt: String?,
  val terminal: Boolean,
)

data class CursorRemoteWorkerSummary(
  val connectedCount: Int,
  val inUseCount: Int,
  val idleCount: Int,
)

data class CursorRemoteWorker(
  val id: String,
  val status: String,
  val poolName: String?,
  val machineName: String?,
  val repositoryUrl: String?,
  val labels: String,
  val lastSeenAt: String?,
)

data class CursorRemotePendingRequest(
  val id: String,
  val repositoryUrl: String?,
  val poolName: String?,
  val labels: String,
  val createdAtMs: Long?,
)

data class CursorRemoteArtifact(
  val agentId: String,
  val path: String,
  val sizeBytes: Long?,
  val updatedAt: String?,
  val downloadUrl: String? = null,
  val downloadExpiresAt: String? = null,
)

data class CursorRemoteUsage(
  val agentId: String,
  val runId: String,
  val inputTokens: Long,
  val outputTokens: Long,
  val cacheReadTokens: Long,
  val cacheWriteTokens: Long,
  val totalTokens: Long,
)

data class CursorRunStreamTarget(
  val agentId: String,
  val runId: String,
  val lastEventId: String?,
)

data class CursorStreamEvent(
  val id: String?,
  val type: String,
  val data: String,
  val retentionSeconds: Long?,
)

data class CursorCreateAgentRequest(
  val prompt: String,
  val repositoryUrl: String?,
  val startingRef: String?,
  val modelId: String?,
  val mode: String,
  val targetKind: String?,
  val targetName: String?,
  val autoCreatePr: Boolean,
)

data class CursorCreateAgentResult(
  val agent: CursorRemoteAgent,
  val run: CursorRemoteRun?,
)

data class CursorControlPlaneState(
  val accounts: List<CursorAccountEntity> = emptyList(),
  val models: List<CursorModelEntity> = emptyList(),
  val repositories: List<CursorRepositoryEntity> = emptyList(),
  val agents: List<CursorAgentEntity> = emptyList(),
  val runs: List<CursorRunEntity> = emptyList(),
  val workerSummary: CursorWorkerSummaryEntity? = null,
  val workers: List<CursorWorkerEntity> = emptyList(),
  val pendingRequests: List<CursorPendingRequestEntity> = emptyList(),
  val artifacts: List<CursorArtifactEntity> = emptyList(),
  val usage: List<CursorUsageEntity> = emptyList(),
  val selectedAgentId: String? = null,
  val selectedRunId: String? = null,
  val refreshing: Boolean = false,
  val linking: Boolean = false,
  val errorMessage: String? = null,
) {
  val userLinked: Boolean = accounts.any { it.accountType == CursorAccountKind.User.storageKey && it.linked }
  val serviceAccountLinked: Boolean =
    accounts.any { it.accountType == CursorAccountKind.ServiceAccount.storageKey && it.linked }
  val webLinked: Boolean = accounts.any { it.accountType == CursorAccountKind.Web.storageKey && it.linked }
  val anyLinked: Boolean = userLinked || serviceAccountLinked || webLinked
  val selectedAgent: CursorAgentEntity? = agents.firstOrNull { it.id == selectedAgentId } ?: agents.firstOrNull()
  val selectedRun: CursorRunEntity? =
    selectedRunId?.let { id -> runs.firstOrNull { it.id == id } }
      ?: selectedAgent?.latestRunId?.let { id -> runs.firstOrNull { it.id == id } }
      ?: runs.firstOrNull { it.agentId == selectedAgent?.id }
  val selectedUsage: CursorUsageEntity? =
    selectedRun?.let { run -> usage.firstOrNull { it.agentId == run.agentId && it.runId == run.id } }
}

class CursorApiException(
  message: String,
  val statusCode: Int? = null,
) : Exception(message)
