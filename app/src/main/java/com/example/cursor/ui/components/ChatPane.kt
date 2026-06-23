package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ConversationMessage
import com.example.cursor.model.ConversationState
import com.example.cursor.model.MessageAuthor
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatPane(
  conversation: ConversationState,
  onWorkbenchSelected: (WorkbenchKind) -> Unit,
  modifier: Modifier = Modifier,
  inlineWorkbench: @Composable (() -> Unit)? = null,
) {
  LazyColumn(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item {
      Text(conversation.title, style = MaterialTheme.typography.displaySmall)
    }
    items(conversation.messages, key = { it.id }) { message ->
      MessageBubble(message)
    }
    item {
      FlowRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        conversation.workbenchShortcuts.forEach { shortcut ->
          Surface(
            onClick = { onWorkbenchSelected(shortcut.kind) },
            shape = CursorShape.Card,
            color = CursorColors.Surface,
            border = BorderStroke(1.dp, CursorColors.Stroke),
          ) {
            Column(Modifier.padding(12.dp)) {
              Text(shortcut.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              Text(shortcut.detail, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
            }
          }
        }
      }
    }
    if (inlineWorkbench != null) {
      item { inlineWorkbench() }
    }
  }
}

@Composable
private fun MessageBubble(message: ConversationMessage) {
  Row(verticalAlignment = Alignment.Top) {
    Avatar(message.author)
    Spacer(Modifier.size(CursorSpacing.Md))
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = CursorShape.Card,
      color = if (message.author == MessageAuthor.User) CursorColors.SurfaceSoft else CursorColors.Surface,
      border = BorderStroke(1.dp, CursorColors.Stroke),
    ) {
      Column(Modifier.padding(CursorSpacing.Lg), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(message.author.name, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
          Spacer(Modifier.weight(1f))
          Text(message.timestamp, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
        }
        Text(message.body, style = MaterialTheme.typography.bodyLarge)
      }
    }
  }
}

@Composable
private fun Avatar(author: MessageAuthor) {
  val isCursor = author == MessageAuthor.Cursor
  Surface(
    modifier =
      Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(if (isCursor) CursorColors.Ink else CursorColors.Blue),
    shape = CircleShape,
    color = if (isCursor) CursorColors.Ink else CursorColors.Blue,
  ) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = if (isCursor) "C" else "You",
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
      )
    }
  }
}
