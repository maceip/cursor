package com.example.cursor.model

data class CursorThreadState(
  val threadId: String,
  val workspaceName: String,
  val modelName: String,
  val messages: List<ChatMessage>,
  val composer: ComposerState,
)

data class ChatMessage(
  val id: String,
  val author: MessageAuthor,
  val text: String,
  val timestampLabel: String,
)

enum class MessageAuthor {
  User,
  Cursor,
}

data class ComposerState(
  val placeholder: String,
  val attachments: List<AttachmentChipModel> = emptyList(),
  val promptTokens: List<PromptToken> = emptyList(),
  val autocompleteSuggestions: List<AutocompleteSuggestion> = emptyList(),
  val isVoiceReady: Boolean = true,
)

data class AttachmentChipModel(
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
  PlainText,
}

data class AutocompleteSuggestion(
  val label: String,
  val detail: String,
)
