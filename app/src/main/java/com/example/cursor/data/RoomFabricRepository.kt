package com.example.cursor.data

import android.content.Context
import com.example.cursor.data.cursor.CursorRedactor
import com.example.cursor.data.local.database.CursorDatabase
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.ComposerState
import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload
import com.example.cursor.model.FabricTopologyProjector
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.FabricUpstreamPayload
import com.example.cursor.model.FabricUpstreamSignal
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchShortcut
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class RoomFabricRepository(
  private val ledger: FabricEventLedger,
  scope: CoroutineScope,
  private val streamClient: FabricStreamClient = OfflineFabricStreamClient,
  private val threadId: String = FabricDefaults.DefaultThreadId,
  seedOnEmpty: Boolean = false,
) : FabricRepository {
  private val repositoryScope = scope
  private val demoMode = seedOnEmpty
  private var connectionJob: Job? = null
  private val initialPackets = if (demoMode) seedFabricPackets() else emptyList()

  private val _conversation = MutableStateFlow(if (demoMode) seedConversation(threadId) else emptyConversation(threadId))
  override val conversation: StateFlow<ConversationState> = _conversation.asStateFlow()

  private val _activeWorkbench =
    MutableStateFlow(if (demoMode) seedWorkbench(threadId, WorkbenchKind.Spec) else emptyWorkbench(threadId, WorkbenchKind.Spec))
  override val activeWorkbench: StateFlow<WorkbenchState> = _activeWorkbench.asStateFlow()

  override val packets: StateFlow<List<FabricPacket>> =
    ledger.packets().stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialPackets)

  override val topology: StateFlow<FabricTopologyState> =
    packets
      .map { FabricTopologyProjector.fromPackets(it) }
      .stateIn(scope, SharingStarted.WhileSubscribed(5_000), FabricTopologyProjector.fromPackets(initialPackets))

  init {
    if (demoMode) {
      scope.launch {
        if (ledger.latestSequenceNumber() == 0L) {
          ledger.appendAll(seedFabricPackets())
        }
      }
    }
  }

  override fun connect() {
    connectionJob?.cancel()
    connectionJob =
      repositoryScope.launch(Dispatchers.IO) {
        val lastKnownSequence = maxOf(ledger.latestSequenceNumber(), packets.value.maxOfOrNull { it.sequenceNumber } ?: 0L)
        streamClient
          .packetsAfter(lastKnownSequence)
          .catch { failure ->
            if (failure is CancellationException) throw failure
            appendLocalErrorPacket("Fabric stream failed: ${failure.message ?: failure::class.simpleName.orEmpty()}")
          }
          .collect { packet -> ledger.append(packet) }
      }
  }

  override fun disconnect() {
    connectionJob?.cancel()
    connectionJob = null
  }

  override fun openWorkbench(kind: WorkbenchKind) {
    _activeWorkbench.value = if (demoMode) seedWorkbench(threadId, kind) else emptyWorkbench(threadId, kind)
  }

  override fun submitUserMessage(text: String) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return

    val timestampMs = System.currentTimeMillis()
    _conversation.value = _conversation.value.withUserMessage(trimmed, timestampMs)
    sendSignal(FabricUpstreamPayload.UserMessage(trimmed), timestampMs)
  }

  override fun approveInteraction(interactionId: String, approved: Boolean, messageOverride: String?) {
    sendSignal(
      FabricUpstreamPayload.ActionApproval(
        interactionId = interactionId,
        approved = approved,
        messageOverride = messageOverride,
      )
    )
  }

  override fun cancelAgentRun(agentRunId: String) {
    sendSignal(FabricUpstreamPayload.CancelTask(agentRunId))
  }

  override fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>> = ledger.packetsAfter(sequenceNumber)

  override suspend fun appendPacket(packet: FabricPacket) {
    ledger.append(packet)
  }

  private fun sendSignal(payload: FabricUpstreamPayload, timestampMs: Long = System.currentTimeMillis()) {
    repositoryScope.launch(Dispatchers.IO) {
      try {
        streamClient.send(
          FabricUpstreamSignal(
            signalId = "signal-${UUID.randomUUID()}",
            timestampMs = timestampMs,
            payload = payload,
          ),
        )
      } catch (failure: CancellationException) {
        throw failure
      } catch (failure: Throwable) {
        appendLocalErrorPacket("Cursor request failed: ${failure.message ?: failure::class.simpleName.orEmpty()}")
      }
    }
  }

  private suspend fun appendLocalErrorPacket(message: String) {
    val nextSequence = ledger.latestSequenceNumber() + 1
    ledger.append(
      FabricPacket(
        packetId = "local-error-${UUID.randomUUID()}",
        sequenceNumber = nextSequence,
        timestampMs = System.currentTimeMillis(),
        hostId = "cursor-api",
        workspaceId = threadId,
        agentRunId = "local",
        payload = FabricPayload.StatusChanged(AgentStatus.Idle, CursorRedactor.redact(message).take(MaxLocalErrorLength)),
      ),
    )
  }

  companion object {
    private const val MaxLocalErrorLength = 240

    fun create(
      context: Context,
      scope: CoroutineScope,
      streamClient: FabricStreamClient = OfflineFabricStreamClient,
      seedOnEmpty: Boolean = false,
    ): RoomFabricRepository =
      RoomFabricRepository(
        ledger = RoomFabricEventLedger(CursorDatabase.getInstance(context)),
        scope = scope,
        streamClient = streamClient,
        seedOnEmpty = seedOnEmpty,
      )
  }
}

private fun emptyConversation(threadId: String): ConversationState =
  ConversationState(
    threadId = threadId,
    title = "Cursor",
    workspaceName = "Cursor Cloud",
    modelName = "Cursor agent",
    messages = emptyList(),
    composer =
      ComposerState(
        promptHint = "Ask Cursor to start or continue work",
        attachments = emptyList(),
        tokens = emptyList(),
        quickActions = listOf("Search", "Files", "Think", "More"),
        isVoiceReady = true,
      ),
    workbenchShortcuts = WorkbenchKind.entries.map { WorkbenchShortcut(it, it.label, it.productionDetail) },
  )

private fun emptyWorkbench(threadId: String, kind: WorkbenchKind): WorkbenchState =
  WorkbenchState(
    threadId = threadId,
    kind = kind,
    status = "Ready",
    title = kind.label,
    summary = "Live Cursor work will appear here.",
    spec = null,
    codeReview = null,
    handoff = null,
    artifact = null,
    draft = null,
  )

private val WorkbenchKind.productionDetail: String
  get() =
    when (this) {
      WorkbenchKind.Spec -> "Plan"
      WorkbenchKind.CodeReview -> "Diff"
      WorkbenchKind.Handoff -> "Desktop"
      WorkbenchKind.Artifact -> "Preview"
      WorkbenchKind.Writing -> "Draft"
    }
