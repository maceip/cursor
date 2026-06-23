package com.example.cursor.model

data class ConversationState(
  val threadId: String,
  val title: String,
  val workspaceName: String,
  val modelName: String,
  val messages: List<ConversationMessage>,
  val composer: ComposerState,
  val workbenchShortcuts: List<WorkbenchShortcut>,
)

data class ConversationMessage(
  val id: String,
  val author: MessageAuthor,
  val body: String,
  val timestamp: String,
)

enum class MessageAuthor {
  User,
  Cursor,
}

data class ComposerState(
  val promptHint: String,
  val attachments: List<AttachmentChip>,
  val tokens: List<PromptToken>,
  val quickActions: List<String>,
  val isVoiceReady: Boolean,
)

data class AttachmentChip(
  val label: String,
  val detail: String,
)

data class PromptToken(
  val value: String,
  val kind: PromptTokenKind,
)

enum class PromptTokenKind {
  Mention,
  SlashCommand,
  File,
}

data class WorkbenchShortcut(
  val kind: WorkbenchKind,
  val title: String,
  val detail: String,
)
