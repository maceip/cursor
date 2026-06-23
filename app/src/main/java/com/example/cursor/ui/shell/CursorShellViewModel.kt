package com.example.cursor.ui.shell

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursor.data.FabricRepository
import com.example.cursor.data.FabricStreamClient
import com.example.cursor.data.InMemoryFabricRepository
import com.example.cursor.data.OfflineFabricStreamClient
import com.example.cursor.data.RoomFabricRepository
import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class CursorShellViewModel(
  repositoryFactory: (CoroutineScope) -> FabricRepository = { InMemoryFabricRepository() },
) : ViewModel() {
  private val repository: FabricRepository = repositoryFactory(viewModelScope)

  val conversation: StateFlow<ConversationState> = repository.conversation
  val activeWorkbench: StateFlow<WorkbenchState> = repository.activeWorkbench
  val topology: StateFlow<FabricTopologyState> =
    repository.topology.sample(30.milliseconds).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.topology.value)

  private val _navigationRequests = MutableStateFlow<WorkbenchKind?>(null)
  val navigationRequests: StateFlow<WorkbenchKind?> = _navigationRequests.asStateFlow()

  fun connect() {
    repository.connect()
  }

  fun disconnect() {
    repository.disconnect()
  }

  fun openWorkbench(kind: WorkbenchKind) {
    repository.openWorkbench(kind)
    _navigationRequests.value = kind
  }

  fun submitUserMessage(text: String) {
    repository.submitUserMessage(text)
  }

  fun approveInteraction(interactionId: String, approved: Boolean, messageOverride: String? = null) {
    repository.approveInteraction(interactionId, approved, messageOverride)
  }

  fun cancelAgentRun(agentRunId: String) {
    repository.cancelAgentRun(agentRunId)
  }

  fun navigationHandled() {
    _navigationRequests.value = null
  }

  override fun onCleared() {
    repository.disconnect()
    super.onCleared()
  }

  companion object {
    fun roomBacked(
      context: Context,
      streamClient: FabricStreamClient = OfflineFabricStreamClient,
    ): CursorShellViewModel =
      CursorShellViewModel { scope ->
        RoomFabricRepository.create(
          context = context.applicationContext,
          scope = scope,
          streamClient = streamClient,
        )
      }
  }
}
