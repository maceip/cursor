package com.example.cursor.nav

import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

enum class PaneRole {
  Conversation,
  Workbench,
}

object CursorSceneMetadata {
  object PaneRoleKey : NavMetadataKey<PaneRole>

  object ThreadIdKey : NavMetadataKey<String>

  fun conversation(threadId: String) =
    metadata {
      put(PaneRoleKey, PaneRole.Conversation)
      put(ThreadIdKey, threadId)
    }

  fun workbench(threadId: String) =
    metadata {
      put(PaneRoleKey, PaneRole.Workbench)
      put(ThreadIdKey, threadId)
    }
}
