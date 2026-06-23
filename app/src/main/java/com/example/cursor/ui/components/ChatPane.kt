package com.example.cursor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.ChatMessage
import com.example.cursor.model.CursorThreadState
import com.example.cursor.model.MessageAuthor
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ChatPane(
  thread: CursorThreadState,
  modifier: Modifier = Modifier,
  inlineWorkbench: (@Composable () -> Unit)? = null,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg)) {
    thread.messages.forEach { message -> ChatMessageRow(message) }
    inlineWorkbench?.let {
      Spacer(Modifier.height(CursorSpacing.Sm))
      it()
    }
  }
}

@Composable
private fun ChatMessageRow(message: ChatMessage) {
  Row(Modifier.fillMaxWidth()) {
    CursorAvatar(if (message.author == MessageAuthor.Cursor) "Cursor" else "You")
    Spacer(Modifier.width(CursorSpacing.Md))
    Column(Modifier.weight(1f)) {
      MetaRow(
        label = if (message.author == MessageAuthor.Cursor) "Cursor" else "You",
        trailing = message.timestampLabel,
      )
      Spacer(Modifier.height(CursorSpacing.Sm))
      val background = if (message.author == MessageAuthor.User) CursorColors.Soft.copy(alpha = 0.7f) else CursorColors.Card
      CursorCard(Modifier.fillMaxWidth()) {
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyLarge,
          color = CursorColors.Ink,
        )
      }
    }
  }
}
