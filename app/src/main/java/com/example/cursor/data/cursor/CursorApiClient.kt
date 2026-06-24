package com.example.cursor.data.cursor

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject

class CursorApiClient(
  private val baseUrl: String = "https://api.cursor.com",
) {
  suspend fun validateKey(token: String): CursorKeyInfo =
    requestJson(token = token, path = "/v1/me").let { json ->
      CursorKeyInfo(
        apiKeyName = json.optString("apiKeyName", "Cursor API key"),
        createdAt = json.optStringOrNull("createdAt"),
        userId = if (json.has("userId")) json.optLong("userId") else null,
        userEmail = json.optStringOrNull("userEmail"),
        userFirstName = json.optStringOrNull("userFirstName"),
        userLastName = json.optStringOrNull("userLastName"),
      )
    }

  suspend fun listModels(token: String): List<CursorRemoteModel> =
    requestJson(token = token, path = "/v1/models")
      .optJSONArray("items")
      .orEmptyObjects()
      .map { item ->
        val defaultFromVariant =
          item.optJSONArray("variants").orEmptyObjects().any { variant -> variant.optBoolean("isDefault", false) }
        CursorRemoteModel(
          id = item.optString("id"),
          displayName = item.optString("displayName", item.optString("id")),
          description = item.optString("description", ""),
          isDefault = defaultFromVariant,
        )
      }
      .filter { it.id.isNotBlank() }

  suspend fun listRepositories(token: String): List<CursorRemoteRepository> =
    requestJson(token = token, path = "/v1/repositories")
      .optJSONArray("items")
      .orEmptyObjects()
      .mapNotNull { item -> item.optStringOrNull("url")?.let(::CursorRemoteRepository) }

  suspend fun listAgents(token: String, limit: Int = 50): List<CursorRemoteAgent> =
    requestJson(token = token, path = "/v1/agents?limit=$limit")
      .optJSONArray("items")
      .orEmptyObjects()
      .mapNotNull(::parseAgent)

  suspend fun listRuns(token: String, agentId: String, limit: Int = 20): List<CursorRemoteRun> =
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/runs?limit=$limit")
      .optJSONArray("items")
      .orEmptyObjects()
      .mapNotNull { parseRun(it, fallbackAgentId = agentId) }

  suspend fun getRun(token: String, agentId: String, runId: String): CursorRemoteRun? =
    parseRun(requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/runs/${runId.encodePath()}"), fallbackAgentId = agentId)

  suspend fun createAgent(token: String, request: CursorCreateAgentRequest): CursorCreateAgentResult {
    val body =
      JSONObject()
        .put("prompt", JSONObject().put("text", request.prompt))
        .put("mode", request.mode)
        .put("autoCreatePR", request.autoCreatePr)

    request.modelId?.takeIf { it.isNotBlank() }?.let { body.put("model", JSONObject().put("id", it)) }
    request.targetKind?.takeIf { it.isNotBlank() }?.let { kind ->
      val env = JSONObject().put("type", kind)
      request.targetName?.takeIf { it.isNotBlank() }?.let { env.put("name", it) }
      body.put("env", env)
    }
    request.repositoryUrl?.takeIf { it.isNotBlank() }?.let { repoUrl ->
      val repo = JSONObject().put("url", repoUrl)
      request.startingRef?.takeIf { it.isNotBlank() }?.let { repo.put("startingRef", it) }
      body.put("repos", JSONArray().put(repo))
    }

    val json = requestJson(token = token, path = "/v1/agents", method = "POST", body = body)
    val agent = json.optJSONObject("agent")?.let(::parseAgent) ?: throw CursorApiException("Create agent response omitted agent")
    return CursorCreateAgentResult(agent = agent, run = json.optJSONObject("run")?.let { parseRun(it, fallbackAgentId = agent.id) })
  }

  suspend fun createRun(token: String, agentId: String, prompt: String): CursorRemoteRun {
    val body = JSONObject().put("prompt", JSONObject().put("text", prompt))
    val json = requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/runs", method = "POST", body = body)
    return parseRun(json, fallbackAgentId = agentId)
      ?: json.optJSONObject("run")?.let { parseRun(it, fallbackAgentId = agentId) }
      ?: throw CursorApiException("Create run response omitted run")
  }

  suspend fun cancelRun(token: String, agentId: String, runId: String) {
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/runs/${runId.encodePath()}/cancel", method = "POST")
  }

  suspend fun respondToInteraction(
    token: String,
    agentId: String,
    runId: String,
    interactionId: String,
    approved: Boolean,
    messageOverride: String?,
  ) {
    val body = JSONObject().put("approved", approved)
    messageOverride?.takeIf { it.isNotBlank() }?.let { body.put("messageOverride", it) }
    requestJson(
      token = token,
      path =
        "/v1/agents/${agentId.encodePath()}/runs/${runId.encodePath()}/interactions/${interactionId.encodePath()}/respond",
      method = "POST",
      body = body,
    )
  }

  suspend fun archiveAgent(token: String, agentId: String) {
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/archive", method = "POST")
  }

  suspend fun unarchiveAgent(token: String, agentId: String) {
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/unarchive", method = "POST")
  }

  suspend fun listArtifacts(token: String, agentId: String): List<CursorRemoteArtifact> =
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/artifacts")
      .optJSONArray("items")
      .orEmptyObjects()
      .mapNotNull { item ->
        val path = item.optStringOrNull("path") ?: return@mapNotNull null
        CursorRemoteArtifact(
          agentId = agentId,
          path = path,
          sizeBytes = if (item.has("sizeBytes")) item.optLong("sizeBytes") else null,
          updatedAt = item.optStringOrNull("updatedAt"),
        )
      }

  suspend fun artifactDownloadUrl(token: String, agentId: String, path: String): CursorRemoteArtifact =
    requestJson(
        token = token,
        path = "/v1/agents/${agentId.encodePath()}/artifacts/download?path=${path.encodeQuery()}",
      )
      .let { json ->
        CursorRemoteArtifact(
          agentId = agentId,
          path = path,
          sizeBytes = null,
          updatedAt = null,
          downloadUrl = json.optStringOrNull("url"),
          downloadExpiresAt = json.optStringOrNull("expiresAt"),
        )
      }

  suspend fun usage(token: String, agentId: String): List<CursorRemoteUsage> =
    requestJson(token = token, path = "/v1/agents/${agentId.encodePath()}/usage")
      .optJSONArray("runs")
      .orEmptyObjects()
      .mapNotNull { run ->
        val runId = run.optStringOrNull("id") ?: return@mapNotNull null
        val usage = run.optJSONObject("usage") ?: JSONObject()
        val input = usage.optLong("inputTokens", 0)
        val output = usage.optLong("outputTokens", 0)
        val cacheRead = usage.optLong("cacheReadTokens", 0)
        val cacheWrite = usage.optLong("cacheWriteTokens", 0)
        CursorRemoteUsage(
          agentId = agentId,
          runId = runId,
          inputTokens = input,
          outputTokens = output,
          cacheReadTokens = cacheRead,
          cacheWriteTokens = cacheWrite,
          totalTokens = usage.optLong("totalTokens", input + output + cacheRead + cacheWrite),
        )
      }

  suspend fun workerSummary(token: String): CursorRemoteWorkerSummary {
    val json = requestJson(token = token, path = "/v0/private-workers/summary")
    val team = json.optJSONObject("teamSummary") ?: json
    val connected = team.optInt("totalConnected", team.optInt("connected", 0))
    val inUse = team.optInt("inUse", team.optInt("inUseCount", 0))
    val idle = team.optInt("idle", (connected - inUse).coerceAtLeast(0))
    return CursorRemoteWorkerSummary(connectedCount = connected, inUseCount = inUse, idleCount = idle)
  }

  suspend fun listWorkers(token: String, status: String = "all", limit: Int = 100): List<CursorRemoteWorker> {
    val json = requestJson(token = token, path = "/v0/private-workers?status=${status.encodeQuery()}&limit=$limit")
    val workers = json.optJSONArray("workers") ?: json.optJSONArray("items") ?: json.optJSONArray("data")
    return workers
      .orEmptyObjects()
      .mapNotNull { item ->
        val id = item.optStringOrNull("id") ?: item.optStringOrNull("workerId") ?: return@mapNotNull null
        val labels = item.optJSONArray("labels")
        CursorRemoteWorker(
          id = id,
          status = item.optString("status", "unknown"),
          poolName = item.optStringOrNull("pool") ?: item.optStringOrNull("poolName") ?: labels.labelValue("pool"),
          machineName = item.optStringOrNull("machine") ?: item.optStringOrNull("machineName"),
          repositoryUrl = item.optStringOrNull("repoUrl") ?: item.optStringOrNull("repository") ?: labels.labelValue("repo"),
          labels = labels.compactLabels(),
          lastSeenAt = item.optStringOrNull("lastSeenAt") ?: item.optStringOrNull("updatedAt"),
        )
      }
  }

  suspend fun pendingPoolRequests(token: String, limit: Int = 100): List<CursorRemotePendingRequest> {
    val json = requestJson(token = token, path = "/v0/private-workers/pending-requests?limit=$limit")
    return (json.optJSONArray("requests") ?: json.optJSONArray("items"))
      .orEmptyObjects()
      .mapNotNull { item ->
        val id = item.optStringOrNull("id") ?: return@mapNotNull null
        val labels = item.optJSONArray("labels")
        CursorRemotePendingRequest(
          id = id,
          repositoryUrl = item.optStringOrNull("repoUrl") ?: labels.labelValue("repo"),
          poolName = labels.labelValue("pool") ?: item.optStringOrNull("pool"),
          labels = labels.compactLabels(),
          createdAtMs = if (item.has("createdAtMs")) item.optLong("createdAtMs") else null,
        )
      }
  }

  fun streamRun(token: String, target: CursorRunStreamTarget): Flow<CursorStreamEvent> =
    flow {
        val path = "/v1/agents/${target.agentId.encodePath()}/runs/${target.runId.encodePath()}/stream"
        val connection = openConnection(token, path, method = "GET", accept = "text/event-stream")
        target.lastEventId?.takeIf { it.isNotBlank() }?.let { connection.setRequestProperty("Last-Event-ID", it) }
        connection.readTimeout = 0
        val code = connection.responseCode
        if (code !in 200..299) {
          throw CursorApiException(readError(connection, code), code)
        }

        val retentionSeconds = connection.getHeaderField("X-Cursor-Stream-Retention-Seconds")?.toLongOrNull()
        val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
        var id: String? = null
        var type = "message"
        val data = StringBuilder()

        suspend fun dispatch() {
          if (data.isEmpty() && type == "message") return
          emit(CursorStreamEvent(id = id, type = type, data = data.toString().trimEnd(), retentionSeconds = retentionSeconds))
          id = null
          type = "message"
          data.clear()
        }

        try {
          while (true) {
            val line = reader.readLine() ?: break
            when {
              line.isEmpty() -> dispatch()
              line.startsWith("id:") -> id = line.removePrefix("id:").trim()
              line.startsWith("event:") -> type = line.removePrefix("event:").trim()
              line.startsWith("data:") -> data.append(line.removePrefix("data:").trim()).append('\n')
            }
          }
          dispatch()
        } finally {
          connection.disconnect()
        }
      }
      .flowOn(Dispatchers.IO)

  private fun parseAgent(item: JSONObject): CursorRemoteAgent? {
    val id = item.optStringOrNull("id") ?: return null
    val env = item.optJSONObject("env")
    val firstRepo = item.optJSONArray("repos")?.optJSONObject(0)
    return CursorRemoteAgent(
      id = id,
      name = item.optString("name", id),
      status = item.optString("status", "UNKNOWN"),
      url = item.optString("url", ""),
      latestRunId = item.optStringOrNull("latestRunId"),
      repositoryUrl = firstRepo?.optStringOrNull("url"),
      targetKind = env?.optStringOrNull("type"),
      targetName = env?.optStringOrNull("name"),
      createdAt = item.optStringOrNull("createdAt"),
      updatedAt = item.optStringOrNull("updatedAt"),
    )
  }

  private fun parseRun(item: JSONObject, fallbackAgentId: String? = null): CursorRemoteRun? {
    val id = item.optStringOrNull("id") ?: return null
    val status = item.optString("status", "UNKNOWN")
    return CursorRemoteRun(
      id = id,
      agentId = item.optStringOrNull("agentId") ?: fallbackAgentId.orEmpty(),
      status = status,
      result = item.optStringOrNull("result") ?: item.optStringOrNull("text"),
      createdAt = item.optStringOrNull("createdAt"),
      updatedAt = item.optStringOrNull("updatedAt"),
      terminal = status in TerminalRunStatuses,
    )
  }

  private suspend fun requestJson(
    token: String,
    path: String,
    method: String = "GET",
    body: JSONObject? = null,
    accept: String = "application/json",
  ): JSONObject =
    kotlinx.coroutines.withContext(Dispatchers.IO) {
      val connection = openConnection(token, path, method, accept)
      try {
        if (body != null) {
          connection.doOutput = true
          connection.setRequestProperty("Content-Type", "application/json")
          OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer -> writer.write(body.toString()) }
        }

        val code = connection.responseCode
        if (code !in 200..299) {
          throw CursorApiException(readError(connection, code), code)
        }

        val text = connection.inputStream.readUtf8()
        if (text.isBlank()) JSONObject() else JSONObject(text)
      } finally {
        connection.disconnect()
      }
    }

  private fun openConnection(
    token: String,
    path: String,
    method: String,
    accept: String,
  ): HttpURLConnection =
    (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
      requestMethod = method
      connectTimeout = 15_000
      readTimeout = 30_000
      setRequestProperty("Accept", accept)
      setRequestProperty("Authorization", "Bearer $token")
    }

  private fun readError(connection: HttpURLConnection, code: Int): String {
    val body = connection.errorStream?.readUtf8().orEmpty()
    connection.disconnect()
    return CursorRedactor.redact("Cursor API $code ${body.ifBlank { "request failed" }}")
  }

  private fun InputStream.readUtf8(): String =
    BufferedReader(InputStreamReader(this, Charsets.UTF_8)).use { reader -> reader.readText() }

  private fun String.encodePath(): String = URLEncoder.encode(this, "UTF-8").replace("+", "%20")

  private fun String.encodeQuery(): String = URLEncoder.encode(this, "UTF-8")

  private fun JSONObject.optStringOrNull(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

  private fun JSONArray?.orEmptyObjects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { index -> optJSONObject(index) }

  private fun JSONArray?.labelValue(key: String): String? =
    orEmptyObjects().firstOrNull { it.optString("key") == key }?.optStringOrNull("value")

  private fun JSONArray?.compactLabels(): String =
    orEmptyObjects().joinToString(", ") { label ->
      val key = label.optString("key")
      val value = label.optString("value")
      if (key.isBlank()) value else "$key=$value"
    }

  companion object {
    private val TerminalRunStatuses = setOf("FINISHED", "ERROR", "CANCELLED", "EXPIRED")
  }
}
