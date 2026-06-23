package com.example.cursor.nav

object CursorSceneMetadata {
  const val PaneRole = "paneRole"
  const val ThreadId = "threadId"
  const val WorkbenchKind = "workbenchKind"
}

enum class PaneRole {
  Conversation,
  Workbench,
}

data class CursorPanePair(
  val threadId: String,
  val conversationEntryIndex: Int,
  val workbenchEntryIndex: Int,
  val workbenchKind: WorkbenchKind,
)
