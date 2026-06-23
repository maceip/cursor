package com.example.cursor.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

fun metadataFor(key: NavKey): Map<String, Any> =
  when (key) {
    is ConversationKey ->
      mapOf(
        CursorSceneMetadata.PaneRole to PaneRole.Conversation,
        CursorSceneMetadata.ThreadId to key.threadId,
      )
    is WorkbenchKey ->
      mapOf(
        CursorSceneMetadata.PaneRole to PaneRole.Workbench,
        CursorSceneMetadata.ThreadId to key.threadId,
        CursorSceneMetadata.WorkbenchKind to key.kind,
      )
    else -> emptyMap()
  }

fun conversationEntry(
  key: ConversationKey,
  content: @Composable (ConversationKey) -> Unit,
): NavEntry<NavKey> = NavEntry(key = key, metadata = metadataFor(key)) { content(key) }

fun workbenchEntry(
  key: WorkbenchKey,
  content: @Composable (WorkbenchKey) -> Unit,
): NavEntry<NavKey> = NavEntry(key = key, metadata = metadataFor(key)) { content(key) }
