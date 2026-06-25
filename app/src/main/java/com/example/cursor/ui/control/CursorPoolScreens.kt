package com.example.cursor.ui.control

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cursor.data.cursor.CursorAccountKind
import com.example.cursor.data.cursor.CursorControlPlaneState
import com.example.cursor.data.cursor.CursorCreateAgentRequest
import com.example.cursor.data.local.entity.CursorAgentEntity
import com.example.cursor.data.local.entity.CursorArtifactEntity
import com.example.cursor.data.local.entity.CursorRunEntity
import com.example.cursor.data.local.entity.CursorWorkerEntity
import com.example.cursor.ui.components.CursorChip
import com.example.cursor.ui.components.StatusPill
import com.example.cursor.ui.components.WorkbenchCard
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CursorOnboardingScreen(
  state: CursorControlPlaneState,
  onLinkKey: (CursorAccountKind, String) -> Unit,
  onContinueWithWeb: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var userKey by rememberSaveable { mutableStateOf("") }
  var serviceKey by rememberSaveable { mutableStateOf("") }
  val uriHandler = LocalUriHandler.current

  LazyColumn(
    modifier =
      modifier
        .fillMaxSize()
        .background(CursorColors.Cream),
    contentPadding = PaddingValues(CursorSpacing.Xl),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item {
      WorkbenchCard(title = "Set up Cursor", eyebrow = "Connect your account") {
        Text(
          "This build starts empty now. Link your Cursor account from your phone to load real agents, repositories, runs, and artifacts.",
          color = CursorColors.Muted,
        )
        Text(
          "If you are already signed in to Cursor on the web, you can open Cursor in your browser and mark setup complete now. Paste a user API key later when you want this app to sync agents directly.",
          color = CursorColors.Muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
          Button(
            onClick = {
              uriHandler.openUri(CursorDashboardUrl)
              onContinueWithWeb()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CursorColors.Ink),
          ) {
            Text("Use Cursor web")
          }
          OutlinedButton(onClick = { uriHandler.openUri(CursorApiKeysUrl) }) {
            Text("Get API key")
          }
        }
        CredentialField(
          label = "User API key",
          value = userKey,
          onValueChange = { userKey = it },
          buttonText = "Link user",
          enabled = !state.linking,
          onSubmit = { onLinkKey(CursorAccountKind.User, userKey) },
        )
        CredentialField(
          label = "Pool service-account key (optional)",
          value = serviceKey,
          onValueChange = { serviceKey = it },
          buttonText = "Link pool",
          enabled = !state.linking,
          onSubmit = { onLinkKey(CursorAccountKind.ServiceAccount, serviceKey) },
        )
        if (state.errorMessage != null) Text(state.errorMessage, color = CursorColors.Rust)
      }
    }
  }
}

@Composable
fun CursorPoolHome(
  state: CursorControlPlaneState,
  onRefresh: () -> Unit,
  onLinkKey: (CursorAccountKind, String) -> Unit,
  onContinueWithWeb: () -> Unit,
  onUnlink: (CursorAccountKind) -> Unit,
  onSelectAgent: (String) -> Unit,
  onSelectRun: (String) -> Unit,
  onCreateAgent: (CursorCreateAgentRequest) -> Unit,
  onArchiveAgent: (String) -> Unit,
  onUnarchiveAgent: (String) -> Unit,
  onHydrateArtifactDownload: (String, String) -> Unit,
  console: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
  ) {
    val wide = maxWidth >= 840.dp
    if (wide) {
      Row(Modifier.fillMaxSize()) {
        PoolDashboard(
          state = state,
          onRefresh = onRefresh,
          onLinkKey = onLinkKey,
          onContinueWithWeb = onContinueWithWeb,
          onUnlink = onUnlink,
          onSelectAgent = onSelectAgent,
          onSelectRun = onSelectRun,
          onCreateAgent = onCreateAgent,
          onArchiveAgent = onArchiveAgent,
          onUnarchiveAgent = onUnarchiveAgent,
          onHydrateArtifactDownload = onHydrateArtifactDownload,
          modifier = Modifier.width(380.dp).fillMaxHeight(),
        )
        Spacer(Modifier.width(1.dp).fillMaxHeight().background(CursorColors.Stroke))
        Column(Modifier.weight(1f).fillMaxHeight()) { console() }
      }
    } else {
      Column(Modifier.fillMaxSize()) {
        PoolDashboard(
          state = state,
          onRefresh = onRefresh,
          onLinkKey = onLinkKey,
          onContinueWithWeb = onContinueWithWeb,
          onUnlink = onUnlink,
          onSelectAgent = onSelectAgent,
          onSelectRun = onSelectRun,
          onCreateAgent = onCreateAgent,
          onArchiveAgent = onArchiveAgent,
          onUnarchiveAgent = onUnarchiveAgent,
          onHydrateArtifactDownload = onHydrateArtifactDownload,
          modifier = Modifier.weight(0.45f),
        )
        Spacer(Modifier.height(1.dp).fillMaxWidth().background(CursorColors.Stroke))
        Column(Modifier.weight(0.55f)) { console() }
      }
    }
  }
}

@Composable
private fun PoolDashboard(
  state: CursorControlPlaneState,
  onRefresh: () -> Unit,
  onLinkKey: (CursorAccountKind, String) -> Unit,
  onContinueWithWeb: () -> Unit,
  onUnlink: (CursorAccountKind) -> Unit,
  onSelectAgent: (String) -> Unit,
  onSelectRun: (String) -> Unit,
  onCreateAgent: (CursorCreateAgentRequest) -> Unit,
  onArchiveAgent: (String) -> Unit,
  onUnarchiveAgent: (String) -> Unit,
  onHydrateArtifactDownload: (String, String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.background(CursorColors.Cream),
    contentPadding = PaddingValues(CursorSpacing.Lg),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item {
      WorkbenchCard(
        title = "Self-hosted pool",
        eyebrow = if (state.refreshing) "syncing" else "cached",
        trailing = {
          Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = CursorColors.Ink),
            enabled = !state.refreshing,
          ) {
            Text("Refresh")
          }
        },
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
          SummaryMetric("Connected", state.workerSummary?.connectedCount ?: 0)
          SummaryMetric("In use", state.workerSummary?.inUseCount ?: 0)
          SummaryMetric("Pending", state.workerSummary?.pendingCount ?: state.pendingRequests.size)
        }
        if (state.errorMessage != null) Text(state.errorMessage, color = CursorColors.Rust)
      }
    }

    item { AccountCard(state = state, onLinkKey = onLinkKey, onContinueWithWeb = onContinueWithWeb, onUnlink = onUnlink) }
    item { CreateAgentCard(state = state, onCreateAgent = onCreateAgent) }

    item {
      WorkbenchCard(title = "Workers", eyebrow = "${state.workers.size} connected rows") {
        if (state.workers.isEmpty()) {
          Text("No workers cached yet.", color = CursorColors.Muted)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
            state.workers.take(8).forEach { worker -> WorkerRow(worker) }
          }
        }
      }
    }

    item {
      WorkbenchCard(title = "Agents", eyebrow = "${state.agents.size} cached") {
        if (state.agents.isEmpty()) {
          Text("No agents cached yet.", color = CursorColors.Muted)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
            state.agents.take(8).forEach { agent ->
              AgentRow(
                agent = agent,
                selected = agent.id == state.selectedAgent?.id,
                onClick = { onSelectAgent(agent.id) },
                onArchive = { onArchiveAgent(agent.id) },
                onUnarchive = { onUnarchiveAgent(agent.id) },
              )
            }
          }
        }
      }
    }

    if (state.selectedAgent != null) {
      items(
        items = state.runs.filter { it.agentId == state.selectedAgent.id }.take(6),
        key = { run -> run.id },
      ) { run ->
        RunRow(run = run, selected = run.id == state.selectedRun?.id, onClick = { onSelectRun(run.id) })
      }
    }

    if (state.selectedRun != null || state.artifacts.isNotEmpty()) {
      item {
        WorkbenchCard(title = "Usage", eyebrow = state.selectedRun?.id ?: "selected run") {
          val usage = state.selectedUsage
          if (usage == null) {
            Text("No usage cached yet.", color = CursorColors.Muted)
          } else {
            Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
              SummaryMetric("Input", usage.inputTokens.toInt())
              SummaryMetric("Output", usage.outputTokens.toInt())
              SummaryMetric("Total", usage.totalTokens.toInt())
            }
          }
        }
      }

      item {
        WorkbenchCard(title = "Artifacts", eyebrow = "${state.artifacts.size} cached") {
          val selectedAgentId = state.selectedAgent?.id
          val artifacts = state.artifacts.filter { selectedAgentId == null || it.agentId == selectedAgentId }.take(6)
          if (artifacts.isEmpty()) {
            Text("No artifacts cached yet.", color = CursorColors.Muted)
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
              artifacts.forEach { artifact ->
                ArtifactRow(artifact = artifact, onHydrate = { onHydrateArtifactDownload(artifact.agentId, artifact.path) })
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CredentialField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  buttonText: String,
  enabled: Boolean,
  onSubmit: () -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f),
      label = { Text(label) },
      visualTransformation = PasswordVisualTransformation(),
      singleLine = true,
    )
    Button(
      onClick = onSubmit,
      enabled = enabled,
      colors = ButtonDefaults.buttonColors(containerColor = CursorColors.Ink),
    ) {
      Text(buttonText)
    }
  }
}

@Composable
private fun AccountCard(
  state: CursorControlPlaneState,
  onLinkKey: (CursorAccountKind, String) -> Unit,
  onContinueWithWeb: () -> Unit,
  onUnlink: (CursorAccountKind) -> Unit,
) {
  var userKey by rememberSaveable { mutableStateOf("") }
  var serviceKey by rememberSaveable { mutableStateOf("") }
  val uriHandler = LocalUriHandler.current
  WorkbenchCard(title = "Accounts", eyebrow = "Cursor API") {
    state.accounts.forEach { account ->
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        CursorChip("${account.apiKeyName} - ${account.principal}", selected = true, modifier = Modifier.weight(1f))
        Button(onClick = { onUnlink(account.kind) }) {
          Text("Unlink")
        }
      }
    }
    if (!state.userLinked) {
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        OutlinedButton(onClick = { uriHandler.openUri(CursorApiKeysUrl) }) {
          Text("Get API key")
        }
        if (!state.webLinked) {
          OutlinedButton(
            onClick = {
              uriHandler.openUri(CursorDashboardUrl)
              onContinueWithWeb()
            },
          ) {
            Text("Use web")
          }
        }
      }
      CredentialField(
        label = "User API key",
        value = userKey,
        onValueChange = { userKey = it },
        buttonText = "Link user",
        enabled = !state.linking,
        onSubmit = { onLinkKey(CursorAccountKind.User, userKey) },
      )
    }
    if (!state.serviceAccountLinked) {
      CredentialField(
        label = "Pool service-account key",
        value = serviceKey,
        onValueChange = { serviceKey = it },
        buttonText = "Link pool",
        enabled = !state.linking,
        onSubmit = { onLinkKey(CursorAccountKind.ServiceAccount, serviceKey) },
      )
    }
  }
}

@Composable
private fun CreateAgentCard(
  state: CursorControlPlaneState,
  onCreateAgent: (CursorCreateAgentRequest) -> Unit,
) {
  var prompt by rememberSaveable { mutableStateOf("") }
  var repositoryUrl by rememberSaveable { mutableStateOf("") }
  var startingRef by rememberSaveable { mutableStateOf("main") }
  var targetName by rememberSaveable { mutableStateOf("") }
  val defaultRepo = state.repositories.firstOrNull()?.url.orEmpty()
  val defaultTarget =
    state.pendingRequests.firstOrNull()?.poolName
      ?: state.workers.firstOrNull { !it.poolName.isNullOrBlank() }?.poolName
      ?: targetName

  WorkbenchCard(title = "Start work", eyebrow = "pool target") {
    OutlinedTextField(
      value = prompt,
      onValueChange = { prompt = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Prompt") },
      minLines = 2,
      maxLines = 4,
    )
    OutlinedTextField(
      value = repositoryUrl.ifBlank { defaultRepo },
      onValueChange = { repositoryUrl = it },
      modifier = Modifier.fillMaxWidth(),
      label = { Text("Repository URL") },
      singleLine = true,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      OutlinedTextField(
        value = startingRef,
        onValueChange = { startingRef = it },
        modifier = Modifier.weight(1f),
        label = { Text("Ref") },
        singleLine = true,
      )
      OutlinedTextField(
        value = targetName.ifBlank { defaultTarget.orEmpty() },
        onValueChange = { targetName = it },
        modifier = Modifier.weight(1f),
        label = { Text("Pool") },
        singleLine = true,
      )
    }
    Button(
      onClick = {
        onCreateAgent(
          CursorCreateAgentRequest(
            prompt = prompt,
            repositoryUrl = repositoryUrl.ifBlank { defaultRepo }.ifBlank { null },
            startingRef = startingRef.ifBlank { null },
            modelId = state.models.firstOrNull { it.isDefault }?.id,
            mode = "agent",
            targetKind = if (state.serviceAccountLinked) "pool" else null,
            targetName = targetName.ifBlank { defaultTarget.orEmpty() }.ifBlank { null },
            autoCreatePr = false,
          ),
        )
        prompt = ""
      },
      enabled = prompt.isNotBlank() && state.userLinked,
      colors = ButtonDefaults.buttonColors(containerColor = CursorColors.Ink),
    ) {
      Text("Create agent")
    }
  }
}

@Composable
private fun SummaryMetric(label: String, value: Int) {
  Surface(
    shape = CursorShape.Card,
    color = CursorColors.SurfaceSoft,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(value.toString(), style = MaterialTheme.typography.titleMedium)
      Text(label, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
    }
  }
}

@Composable
private fun WorkerRow(worker: CursorWorkerEntity) {
  Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    StatusPill(worker.status)
    Column(Modifier.weight(1f)) {
      Text(worker.id, style = MaterialTheme.typography.labelMedium)
      Text(listOfNotNull(worker.poolName, worker.machineName, worker.repositoryUrl).joinToString(" - "), color = CursorColors.Muted)
    }
  }
}

@Composable
private fun AgentRow(
  agent: CursorAgentEntity,
  selected: Boolean,
  onClick: () -> Unit,
  onArchive: () -> Unit,
  onUnarchive: () -> Unit,
) {
  Surface(
    onClick = onClick,
    shape = CursorShape.Card,
    color = if (selected) CursorColors.SurfaceSoft else CursorColors.Surface,
    border = BorderStroke(1.dp, if (selected) CursorColors.Blue.copy(alpha = 0.35f) else CursorColors.Stroke),
  ) {
    Row(Modifier.fillMaxWidth().padding(CursorSpacing.Md), horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      StatusPill(agent.status)
      Column(Modifier.weight(1f)) {
        Text(agent.name, style = MaterialTheme.typography.labelLarge)
        Text(agent.repositoryUrl ?: agent.targetName ?: agent.id, color = CursorColors.Muted)
      }
      Button(onClick = if (agent.status.equals("ARCHIVED", ignoreCase = true)) onUnarchive else onArchive) {
        Text(if (agent.status.equals("ARCHIVED", ignoreCase = true)) "Unarchive" else "Archive")
      }
    }
  }
}

@Composable
private fun RunRow(run: CursorRunEntity, selected: Boolean, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = CursorShape.Card,
    color = if (selected) CursorColors.SurfaceSoft else CursorColors.Surface,
    border = BorderStroke(1.dp, if (selected) CursorColors.Blue.copy(alpha = 0.35f) else CursorColors.Stroke),
  ) {
    Column(Modifier.fillMaxWidth().padding(CursorSpacing.Md), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs)) {
      Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        StatusPill(run.status)
        Text(run.id, style = MaterialTheme.typography.labelLarge)
      }
      run.result?.let { Text(it, color = CursorColors.Muted, maxLines = 2) }
    }
  }
}

@Composable
private fun ArtifactRow(artifact: CursorArtifactEntity, onHydrate: () -> Unit) {
  Surface(
    shape = CursorShape.Card,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Row(Modifier.fillMaxWidth().padding(CursorSpacing.Md), horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      Column(Modifier.weight(1f)) {
        Text(artifact.path, style = MaterialTheme.typography.labelLarge)
        Text(
          text = artifact.downloadUrl?.let { "Download link ready" } ?: artifact.sizeBytes?.let { "$it bytes" } ?: "Metadata cached",
          color = CursorColors.Muted,
        )
      }
      Button(onClick = onHydrate) {
        Text(if (artifact.downloadUrl == null) "Sign" else "Refresh")
      }
    }
  }
}

private val com.example.cursor.data.local.entity.CursorAccountEntity.kind: CursorAccountKind
  get() =
    when (accountType) {
      CursorAccountKind.ServiceAccount.storageKey -> CursorAccountKind.ServiceAccount
      CursorAccountKind.Web.storageKey -> CursorAccountKind.Web
      else -> CursorAccountKind.User
    }

private const val CursorDashboardUrl = "https://cursor.com/dashboard"
private const val CursorApiKeysUrl = "https://cursor.com/dashboard/api-keys"
