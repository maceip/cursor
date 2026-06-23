package com.example.cursor.ui.shell

import androidx.lifecycle.ViewModel
import com.example.cursor.data.FabricRepository
import com.example.cursor.data.FakeFabricRepository
import com.example.cursor.model.ConversationState
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CursorShellViewModel(
  private val repository: FabricRepository = FakeFabricRepository(),
) : ViewModel() {
  val conversation: StateFlow<ConversationState> = repository.conversation
  val activeWorkbench: StateFlow<WorkbenchState> = repository.activeWorkbench
  val topology: StateFlow<FabricTopologyState> = repository.topology

  private val _navigationRequests = MutableStateFlow<WorkbenchKind?>(null)
  val navigationRequests: StateFlow<WorkbenchKind?> = _navigationRequests.asStateFlow()

  fun openWorkbench(kind: WorkbenchKind) {
    repository.openWorkbench(kind)
    _navigationRequests.value = kind
  }

  fun navigationHandled() {
    _navigationRequests.value = null
  }
}
