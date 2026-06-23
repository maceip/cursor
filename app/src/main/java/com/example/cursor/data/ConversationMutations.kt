package com.example.cursor.data

import com.example.cursor.model.ConversationMessage
import com.example.cursor.model.ConversationState
import com.example.cursor.model.MessageAuthor
import java.text.DateFormat
import java.util.Date

internal fun ConversationState.withUserMessage(
  text: String,
  timestampMs: Long,
): ConversationState =
  copy(
    messages =
      messages +
        ConversationMessage(
          id = "local-$timestampMs",
          author = MessageAuthor.User,
          body = text,
          timestamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestampMs)),
        )
  )
