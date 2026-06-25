package com.example.cursor.data.cursor

import android.content.Context
import com.example.cursor.data.local.dao.CursorControlPlaneDao
import com.example.cursor.data.local.database.CursorDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CursorControlPlaneRepository(
  private val dao: CursorControlPlaneDao,
  private val authStore: CursorAuthStore,
  private val apiClient: CursorApiClient,
  private val scope: CoroutineScope,
) {
  private val progress = kotlinx.coroutines.flow.MutableStateFlow(CursorControlPlaneProgress())
  private val selectedAgentId = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
  private val selectedRunId = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

  private val coreCache =
    combine(
      dao.observeAccounts(),
      dao.observeModels(),
      dao.observeRepositories(),
      dao.observeAgents(),
      dao.observeRuns(),
    ) { accounts, models, repositories, agents, runs ->
      CoreCache(accounts, models, repositories, agents, runs)
    }

  private val poolCache =
    combine(
      dao.observeWorkerSummary(),
      dao.observeWorkers(),
      dao.observePendingRequests(),
      dao.observeArtifacts(),
      dao.observeUsage(),
    ) { summary, workers, pending, artifacts, usage ->
      PoolCache(summary, workers, pending, artifacts, usage)
    }

  val state: StateFlow<CursorControlPlaneState> =
    combine(coreCache, poolCache, selectedAgentId, selectedRunId, progress) { core, pool, agentId, runId, progress ->
        CursorControlPlaneState(
          accounts = core.accounts,
          models = core.models,
          repositories = core.repositories,
          agents = core.agents,
          runs = core.runs,
          workerSummary = pool.summary,
          workers = pool.workers,
          pendingRequests = pool.pendingRequests,
          artifacts = pool.artifacts,
          usage = pool.usage,
          selectedAgentId = agentId ?: core.agents.firstOrNull()?.id,
          selectedRunId = runId,
          refreshing = progress.refreshing,
          linking = progress.linking,
          errorMessage = progress.errorMessage,
        )
      }
      .stateIn(scope, SharingStarted.WhileSubscribed(5_000), CursorControlPlaneState())

  val streamTarget: StateFlow<CursorRunStreamTarget?> =
    state
      .map { state ->
        val agent = state.selectedAgent ?: return@map null
        val run = state.selectedRun ?: return@map null
        if (run.terminal) return@map null
        CursorRunStreamTarget(agentId = agent.id, runId = run.id, lastEventId = run.lastEventId)
      }
      .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

  fun refresh() {
    scope.launch { refreshAll() }
  }

  fun linkKey(kind: CursorAccountKind, token: String) {
    scope.launch {
      progress.value = progress.value.copy(linking = true, errorMessage = null)
      runCatching {
          val trimmed = token.trim()
          require(trimmed.isNotBlank()) { "Paste a Cursor API key first." }
          val info = apiClient.validateKey(trimmed)
          validateKind(kind, info)
          authStore.saveToken(kind, trimmed)
          dao.clearRemoteCache()
          selectedAgentId.value = null
          selectedRunId.value = null
          dao.upsertAccount(info.toEntity(kind, linked = true, now = System.currentTimeMillis()))
          refreshAll()
        }
        .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
      progress.value = progress.value.copy(linking = false)
    }
  }


  fun continueWithWeb() {
    scope.launch {
      val now = System.currentTimeMillis()
      dao.clearRemoteCache()
      dao.upsertAccount(
        CursorAccountEntity(
          accountType = CursorAccountKind.Web.storageKey,
          apiKeyName = "Cursor web",
          principal = "Browser session",
          isServiceAccount = false,
          verifiedAtMs = now,
          linked = true,
        ),
      )
      progress.value = progress.value.copy(errorMessage = null)
    }
  }

  fun unlink(kind: CursorAccountKind) {
    scope.launch {
      if (kind != CursorAccountKind.Web) authStore.clearToken(kind)
      dao.deleteAccount(kind.storageKey)
      dao.clearRemoteCache()
      selectedAgentId.value = null
      selectedRunId.value = null
      progress.value = progress.value.copy(errorMessage = null)
      refreshAll()
    }
  }

  fun selectAgent(agentId: String) {
    scope.launch {
      selectedAgentId.value = agentId
      selectedRunId.value = dao.latestRunForAgent(agentId)?.id
      refreshAgentDetails(agentId)
    }
  }

  fun selectRun(runId: String) {
    selectedRunId.value = runId
  }

  fun createAgent(request: CursorCreateAgentRequest) {
    scope.launch {
      runCatching { createAgentInternal(request) }
        .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
    }
  }

  fun archiveAgent(agentId: String) {
    scope.launch {
      runCatching {
          val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before archiving agents.")
          apiClient.archiveAgent(token, agentId)
          refreshUserPlane(System.currentTimeMillis())
        }
        .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
    }
  }

  fun unarchiveAgent(agentId: String) {
    scope.launch {
      runCatching {
          val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before unarchiving agents.")
          apiClient.unarchiveAgent(token, agentId)
          refreshUserPlane(System.currentTimeMillis())
        }
        .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
    }
  }

  fun hydrateArtifactDownload(agentId: String, path: String) {
    scope.launch {
      runCatching {
          val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before downloading artifacts.")
          val now = System.currentTimeMillis()
          dao.upsertArtifacts(listOf(apiClient.artifactDownloadUrl(token, agentId, path).toEntity(now)))
        }
        .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
    }
  }

  suspend fun createRunFromComposer(text: String) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return
    val userToken = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before sending prompts.")
    val selected = state.value.selectedAgent
    if (selected == null) {
      createAgentInternal(defaultCreateAgentRequest(trimmed))
      return
    }

    val run = apiClient.createRun(userToken, selected.id, trimmed)
    dao.upsertRuns(listOf(run.toEntity(now = System.currentTimeMillis())))
    selectedAgentId.value = selected.id
    selectedRunId.value = run.id
  }

  suspend fun cancelRun(runId: String) {
    val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before canceling runs.")
    val run = dao.run(runId) ?: return
    apiClient.cancelRun(token, run.agentId, runId)
    val refreshed =
      apiClient.getRun(token, run.agentId, runId)?.toEntity(now = System.currentTimeMillis(), previous = run)
        ?: run.copy(status = "CANCELLED", terminal = true, lastSyncedAtMs = System.currentTimeMillis())
    dao.upsertRuns(listOf(refreshed))
  }

  suspend fun respondToInteraction(interactionId: String, approved: Boolean, messageOverride: String?) {
    val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before responding to approvals.")
    val run = state.value.selectedRun ?: throw CursorApiException("Select a live Cursor run before responding to approvals.")
    apiClient.respondToInteraction(
      token = token,
      agentId = run.agentId,
      runId = run.id,
      interactionId = interactionId,
      approved = approved,
      messageOverride = messageOverride,
    )
    val now = System.currentTimeMillis()
    val refreshed = apiClient.getRun(token, run.agentId, run.id)?.toEntity(now = now, previous = dao.run(run.id))
    if (refreshed != null) dao.upsertRuns(listOf(refreshed))
  }

  suspend fun recordStreamEvent(target: CursorRunStreamTarget, event: CursorStreamEvent) {
    val current = dao.run(target.runId) ?: return
    val data = runCatching { if (event.data.isBlank()) JSONObject() else JSONObject(event.data) }.getOrDefault(JSONObject())
    val nextStatus =
      when (event.type) {
        "status", "result" -> data.optString("status", current.status)
        "error" -> "ERROR"
        "done" -> "FINISHED"
        else -> current.status
      }
    val resultText =
      when (event.type) {
        "result" -> data.optStringOrNull("text") ?: current.result
        "error" -> data.optStringOrNull("message") ?: current.result
        else -> current.result
      }
    dao.upsertRuns(
      listOf(
        current.copy(
          status = nextStatus,
          result = resultText,
          lastEventId = event.id ?: current.lastEventId,
          streamRetentionSeconds = event.retentionSeconds ?: current.streamRetentionSeconds,
          terminal = nextStatus in TerminalStatuses || event.type == "done",
          lastSyncedAtMs = System.currentTimeMillis(),
        ),
      ),
    )
  }

  suspend fun refreshRunAfterStreamExpired(target: CursorRunStreamTarget) {
    val token = authStore.token(CursorAccountKind.User) ?: return
    val now = System.currentTimeMillis()
    apiClient.getRun(token, target.agentId, target.runId)?.let { dao.upsertRuns(listOf(it.toEntity(now))) }
  }

  private suspend fun refreshAll() {
    progress.value = progress.value.copy(refreshing = true, errorMessage = null)
    runCatching {
        withContext(Dispatchers.IO) {
          val now = System.currentTimeMillis()
          refreshUserPlane(now)
          refreshPoolPlane(now)
        }
      }
      .onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
    progress.value = progress.value.copy(refreshing = false)
  }

  private suspend fun refreshUserPlane(now: Long) {
    val token = authStore.token(CursorAccountKind.User) ?: return
    runCatching { dao.replaceModels(apiClient.listModels(token).map { it.toEntity(now) }) }
      .onFailure { progress.value = progress.value.copy(errorMessage = it.userMessage()) }
    runCatching { dao.replaceRepositories(apiClient.listRepositories(token).map { it.toEntity(now) }) }
    val agents = apiClient.listAgents(token).map { it.toEntity(now) }
    dao.replaceAgents(agents)
    val agentIds = agents.map { it.id }.toSet()
    val currentAgentId = selectedAgentId.value
    if (currentAgentId == null || currentAgentId !in agentIds) {
      selectedAgentId.value = agents.firstOrNull()?.id
      selectedRunId.value = null
    }
    agents.take(8).forEach { agent -> refreshAgentDetails(agent.id) }
  }

  private suspend fun refreshAgentDetails(agentId: String) {
    val token = authStore.token(CursorAccountKind.User) ?: return
    val now = System.currentTimeMillis()
    runCatching {
      val previousRuns = dao.runsForAgent(agentId).associateBy { it.id }
      val runs = apiClient.listRuns(token, agentId).map { it.toEntity(now, previousRuns[it.id]) }
      dao.replaceRunsForAgent(agentId, runs)
      val runIds = runs.map { it.id }.toSet()
      val currentRunId = selectedRunId.value
      if (selectedAgentId.value == agentId && (currentRunId == null || currentRunId !in runIds)) {
        selectedRunId.value = runs.firstOrNull()?.id
      }
      dao.replaceArtifactsForAgent(agentId, apiClient.listArtifacts(token, agentId).map { it.toEntity(now) })
      dao.replaceUsageForAgent(agentId, apiClient.usage(token, agentId).map { it.toEntity(now) })
    }.onFailure { failure -> progress.value = progress.value.copy(errorMessage = failure.userMessage()) }
  }

  private suspend fun refreshPoolPlane(now: Long) {
    val token = authStore.token(CursorAccountKind.ServiceAccount) ?: return
    val pending = apiClient.pendingPoolRequests(token).map { it.toEntity(now) }
    val summary = apiClient.workerSummary(token)
    val workers = apiClient.listWorkers(token).map { it.toEntity(now) }
    dao.replacePoolPlane(
      summary = summary.toEntity(pendingCount = pending.size, now = now),
      workers = workers,
      pendingRequests = pending,
    )
  }

  private suspend fun createAgentInternal(request: CursorCreateAgentRequest) {
    val token = authStore.token(CursorAccountKind.User) ?: throw CursorApiException("Link a user API key before creating agents.")
    val result = apiClient.createAgent(token, request)
    val now = System.currentTimeMillis()
    dao.upsertAgents(listOf(result.agent.toEntity(now)))
    result.run?.let { dao.upsertRuns(listOf(it.toEntity(now))) }
    selectedAgentId.value = result.agent.id
    selectedRunId.value = result.run?.id ?: result.agent.latestRunId
  }

  private fun defaultCreateAgentRequest(prompt: String): CursorCreateAgentRequest {
    val current = state.value
    val targetName =
      current.pendingRequests.firstOrNull()?.poolName
        ?: current.workers.firstOrNull { !it.poolName.isNullOrBlank() }?.poolName
    return CursorCreateAgentRequest(
      prompt = prompt,
      repositoryUrl = current.repositories.firstOrNull()?.url,
      startingRef = null,
      modelId = current.models.firstOrNull { it.isDefault }?.id,
      mode = "agent",
      targetKind = if (current.serviceAccountLinked) "pool" else null,
      targetName = targetName,
      autoCreatePr = false,
    )
  }

  private fun validateKind(kind: CursorAccountKind, info: CursorKeyInfo) {
    when (kind) {
      CursorAccountKind.User ->
        require(!info.isServiceAccount) { "That looks like a service-account key. Put it in the pool key slot." }
      CursorAccountKind.ServiceAccount ->
        require(info.isServiceAccount) { "That looks like a user key. Pool management needs a service-account key." }
      CursorAccountKind.Web -> Unit
    }
  }

  private fun Throwable.userMessage(): String = CursorRedactor.redact(message ?: "Cursor request failed.")

  private fun CursorKeyInfo.toEntity(kind: CursorAccountKind, linked: Boolean, now: Long): CursorAccountEntity =
    CursorAccountEntity(
      accountType = kind.storageKey,
      apiKeyName = apiKeyName,
      principal = principal,
      isServiceAccount = isServiceAccount,
      verifiedAtMs = now,
      linked = linked,
    )

  private fun CursorRemoteModel.toEntity(now: Long): CursorModelEntity =
    CursorModelEntity(id = id, displayName = displayName, description = description, isDefault = isDefault, lastSyncedAtMs = now)

  private fun CursorRemoteRepository.toEntity(now: Long): CursorRepositoryEntity {
    val parts = url.removePrefix("https://github.com/").split("/")
    return CursorRepositoryEntity(
      url = url,
      owner = parts.getOrNull(0).orEmpty(),
      name = parts.getOrNull(1).orEmpty().ifBlank { url.substringAfterLast("/") },
      lastSyncedAtMs = now,
    )
  }

  private fun CursorRemoteAgent.toEntity(now: Long): CursorAgentEntity =
    CursorAgentEntity(
      id = id,
      name = name,
      status = status,
      url = url,
      latestRunId = latestRunId,
      repositoryUrl = repositoryUrl,
      targetKind = targetKind,
      targetName = targetName,
      createdAt = createdAt,
      updatedAt = updatedAt,
      lastSyncedAtMs = now,
    )

  private fun CursorRemoteRun.toEntity(now: Long, previous: CursorRunEntity? = null): CursorRunEntity =
    CursorRunEntity(
      id = id,
      agentId = agentId,
      status = status,
      result = result,
      createdAt = createdAt,
      updatedAt = updatedAt,
      lastEventId = previous?.lastEventId,
      streamRetentionSeconds = previous?.streamRetentionSeconds,
      terminal = terminal,
      lastSyncedAtMs = now,
    )

  private fun CursorRemoteWorkerSummary.toEntity(pendingCount: Int, now: Long): CursorWorkerSummaryEntity =
    CursorWorkerSummaryEntity(
      connectedCount = connectedCount,
      inUseCount = inUseCount,
      idleCount = idleCount,
      pendingCount = pendingCount,
      lastSyncedAtMs = now,
    )

  private fun CursorRemoteWorker.toEntity(now: Long): CursorWorkerEntity =
    CursorWorkerEntity(
      id = id,
      status = status,
      poolName = poolName,
      machineName = machineName,
      repositoryUrl = repositoryUrl,
      labels = labels,
      lastSeenAt = lastSeenAt,
      lastSyncedAtMs = now,
    )

  private fun CursorRemotePendingRequest.toEntity(now: Long): CursorPendingRequestEntity =
    CursorPendingRequestEntity(
      id = id,
      repositoryUrl = repositoryUrl,
      poolName = poolName,
      labels = labels,
      createdAtMs = createdAtMs,
      lastSyncedAtMs = now,
    )

  private fun CursorRemoteArtifact.toEntity(now: Long): CursorArtifactEntity =
    CursorArtifactEntity(
      agentId = agentId,
      path = path,
      sizeBytes = sizeBytes,
      updatedAt = updatedAt,
      downloadUrl = downloadUrl,
      downloadExpiresAt = downloadExpiresAt,
      lastSyncedAtMs = now,
    )

  private fun CursorRemoteUsage.toEntity(now: Long): CursorUsageEntity =
    CursorUsageEntity(
      agentId = agentId,
      runId = runId,
      inputTokens = inputTokens,
      outputTokens = outputTokens,
      cacheReadTokens = cacheReadTokens,
      cacheWriteTokens = cacheWriteTokens,
      totalTokens = totalTokens,
      lastSyncedAtMs = now,
    )

  private fun JSONObject.optStringOrNull(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

  private data class CoreCache(
    val accounts: List<CursorAccountEntity>,
    val models: List<CursorModelEntity>,
    val repositories: List<CursorRepositoryEntity>,
    val agents: List<CursorAgentEntity>,
    val runs: List<CursorRunEntity>,
  )

  private data class PoolCache(
    val summary: CursorWorkerSummaryEntity?,
    val workers: List<CursorWorkerEntity>,
    val pendingRequests: List<CursorPendingRequestEntity>,
    val artifacts: List<CursorArtifactEntity>,
    val usage: List<CursorUsageEntity>,
  )

  private data class CursorControlPlaneProgress(
    val refreshing: Boolean = false,
    val linking: Boolean = false,
    val errorMessage: String? = null,
  )

  companion object {
    private val TerminalStatuses = setOf("FINISHED", "ERROR", "CANCELLED", "EXPIRED")

    fun create(context: Context, scope: CoroutineScope): CursorControlPlaneRepository {
      val database = CursorDatabase.getInstance(context.applicationContext)
      return CursorControlPlaneRepository(
        dao = database.cursorControlPlaneDao(),
        authStore = CursorAuthStore(context.applicationContext),
        apiClient = CursorApiClient(),
        scope = scope,
      )
    }
  }
}
