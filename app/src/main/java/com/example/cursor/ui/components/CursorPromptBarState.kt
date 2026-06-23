package com.example.cursor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

// Adapted from PromptBar's Apache-2.0 headless composer model for this app's design system.
class CursorPromptBarState internal constructor(
  initialText: String = "",
  initialAttachments: List<CursorPromptAttachment> = emptyList(),
  var tokenizer: (String) -> Int = ::approximateTokenCount,
  var maxAttachments: Int = 10,
  var maxChars: Int = 32_000,
) {
  private var fieldState by mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
  private var sendOverrideState by mutableStateOf<CursorSendState?>(null)
  private val attachmentsState = mutableStateListOf<CursorPromptAttachment>()

  init {
    attachmentsState.addAll(initialAttachments)
  }

  var fieldValue: TextFieldValue
    get() = fieldState
    set(value) {
      val cappedText = if (value.text.length <= maxChars) value.text else value.text.substring(0, maxChars)
      val cappedSelection =
        if (cappedText.length == value.text.length) {
          value.selection
        } else {
          TextRange(cappedText.length.coerceAtMost(value.selection.end))
        }
      fieldState = value.copy(text = cappedText, selection = cappedSelection)
    }

  val text: String get() = fieldState.text
  val attachments: List<CursorPromptAttachment> get() = attachmentsState
  val charCount: Int get() = text.length
  val tokenCount: Int get() = tokenizer(text)
  val activeTrigger: CursorActiveTrigger get() = detectActiveTrigger(fieldState.text, fieldState.selection.end)

  val sendState: CursorSendState
    get() = sendOverrideState ?: if (text.isBlank() && attachments.isEmpty()) CursorSendState.Disabled else CursorSendState.Ready

  fun forceSendState(state: CursorSendState?) {
    sendOverrideState = state
  }

  fun addAttachment(attachment: CursorPromptAttachment): Boolean {
    if (attachmentsState.any { it.id == attachment.id }) return false
    if (attachmentsState.size >= maxAttachments) return false
    attachmentsState.add(attachment)
    return true
  }

  fun removeAttachment(id: String) {
    attachmentsState.removeAll { it.id == id }
  }

  fun reset() {
    fieldState = TextFieldValue("", selection = TextRange.Zero)
    attachmentsState.clear()
    sendOverrideState = null
  }
}

@Composable
fun rememberCursorPromptBarState(
  initialText: String = "",
  initialAttachments: List<CursorPromptAttachment> = emptyList(),
  tokenizer: (String) -> Int = ::approximateTokenCount,
  maxAttachments: Int = 10,
  maxChars: Int = 32_000,
): CursorPromptBarState =
  remember(maxAttachments, maxChars) {
    CursorPromptBarState(initialText, initialAttachments, tokenizer, maxAttachments, maxChars)
  }

@Immutable
data class CursorPromptAttachment(
  val id: String,
  val displayName: String,
  val kind: CursorAttachmentKind,
  val detail: String? = null,
)

enum class CursorAttachmentKind {
  File,
  Image,
  Voice,
}

enum class CursorSendState {
  Disabled,
  Ready,
  Sending,
  Streaming,
}

sealed class CursorActiveTrigger {
  data object None : CursorActiveTrigger()
  data class Slash(val query: String, val triggerStart: Int) : CursorActiveTrigger()
  data class Mention(val query: String, val triggerStart: Int) : CursorActiveTrigger()
}

fun detectActiveTrigger(text: String, caretPosition: Int): CursorActiveTrigger {
  if (caretPosition <= 0) return CursorActiveTrigger.None
  val safeCaret = caretPosition.coerceAtMost(text.length)
  var index = safeCaret - 1
  while (index >= 0) {
    val char = text[index]
    if (char == '\n' || char == ' ' || char == '\t') return CursorActiveTrigger.None
    if (char == '/' || char == '@') {
      val isBoundary = index == 0 || text[index - 1].let { it == ' ' || it == '\t' || it == '\n' }
      if (!isBoundary) return CursorActiveTrigger.None
      val query = text.substring(index + 1, safeCaret)
      return if (char == '/') CursorActiveTrigger.Slash(query, index) else CursorActiveTrigger.Mention(query, index)
    }
    index--
  }
  return CursorActiveTrigger.None
}

fun approximateTokenCount(text: String): Int {
  if (text.isEmpty()) return 0
  return ((text.length + 3) / 4).coerceAtLeast(1)
}
