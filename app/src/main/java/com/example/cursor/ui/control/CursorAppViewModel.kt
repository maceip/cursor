package com.example.cursor.ui.control

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursor.data.cursor.CursorAccountKind
import com.example.cursor.data.cursor.CursorApiClient
import com.example.cursor.data.cursor.CursorAuthStore
import com.example.cursor.data.cursor.CursorControlPlaneRepository
import com.example.cursor.data.cursor.CursorCreateAgentRequest
import com.example.cursor.data.cursor.CursorFabricBridgeClient
import com.example.cursor.data.local.database.CursorDatabase

class CursorAppViewModel(context: Context) : ViewModel() {
  private val appContext = context.applicationContext
  private val authStore = CursorAuthStore(appContext)
  private val apiClient = CursorApiClient()
  val controlPlane: CursorControlPlaneRepository =
    CursorControlPlaneRepository(
      dao = CursorDatabase.getInstance(appContext).cursorControlPlaneDao(),
      authStore = authStore,
      apiClient = apiClient,
      scope = viewModelScope,
    )
  val fabricStreamClient = CursorFabricBridgeClient(controlPlane, authStore, apiClient)
  val state = controlPlane.state

  init {
    controlPlane.refresh()
  }

  fun linkKey(kind: CursorAccountKind, token: String) {
    controlPlane.linkKey(kind, token)
  }

  fun unlink(kind: CursorAccountKind) {
    controlPlane.unlink(kind)
  }

  fun refresh() {
    controlPlane.refresh()
  }

  fun selectAgent(agentId: String) {
    controlPlane.selectAgent(agentId)
  }

  fun selectRun(runId: String) {
    controlPlane.selectRun(runId)
  }

  fun createAgent(request: CursorCreateAgentRequest) {
    controlPlane.createAgent(request)
  }

  fun archiveAgent(agentId: String) {
    controlPlane.archiveAgent(agentId)
  }

  fun unarchiveAgent(agentId: String) {
    controlPlane.unarchiveAgent(agentId)
  }

  fun hydrateArtifactDownload(agentId: String, path: String) {
    controlPlane.hydrateArtifactDownload(agentId, path)
  }
}
