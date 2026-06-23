package com.example.cursor.nav

enum class PaneRole {
  Conversation,
  Workbench,
}

object CursorSceneMetadata {
  const val PaneRoleKey = "cursor.paneRole"
  const val ThreadIdKey = "cursor.threadId"

  fun conversation(threadId: String) =
    mapOf(
      PaneRoleKey to PaneRole.Conversation,
      ThreadIdKey to threadId,
    )

  fun workbench(threadId: String) =
    mapOf(
      PaneRoleKey to PaneRole.Workbench,
      ThreadIdKey to threadId,
    )

  fun paneRole(metadata: Map<String, Any>): PaneRole? = metadata[PaneRoleKey] as? PaneRole

  fun threadId(metadata: Map<String, Any>): String? = metadata[ThreadIdKey] as? String
}
