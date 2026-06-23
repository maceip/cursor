package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.R
import com.example.cursor.model.ConversationMessage
import com.example.cursor.model.ConversationState
import com.example.cursor.model.MessageAuthor
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.feedback.rememberCursorHaptics
import com.example.cursor.ui.motion.cursorBackMorph
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
  showQuickActions: Boolean = true,
) {
  val haptics = rememberCursorHaptics()
  val latestCursorMessageId = conversation.messages.lastOrNull { it.author == MessageAuthor.Cursor }?.id
  LaunchedEffect(latestCursorMessageId) {
    if (latestCursorMessageId != null) haptics.liveAgentResponse()
  }

  LazyColumn(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md),
  ) {
    items(conversation.messages, key = { it.id }) { message ->
      MessageRow(message)
    }
    if (inlineWorkbench != null) {
      item { inlineWorkbench() }
    }
    if (showQuickActions) {
      item {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
          verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
        ) {
          QuickActionChip("Search", Icons.Outlined.Search)
          QuickActionChip("Files", Icons.Outlined.AttachFile)
          QuickActionChip("Think", Icons.Outlined.AutoAwesome)
          QuickActionChip("", Icons.Outlined.MoreHoriz)
        }
      }
    }
  }
}

@Composable
private fun MessageRow(message: ConversationMessage) {
  Row(
    modifier = Modifier.cursorBackMorph(scaleXTarget = 0.98f, scaleYTarget = 0.94f, alphaTarget = 0.82f, translateY = 8f),
    verticalAlignment = Alignment.Top,
  ) {
    Avatar(message.author)
    Spacer(Modifier.size(CursorSpacing.Sm))
    if (message.author == MessageAuthor.User) {
      UserBubble(message, Modifier.weight(1f))
    } else {
      AssistantMessage(message, Modifier.weight(1f))
    }
  }
}

@Composable
private fun UserBubble(message: ConversationMessage, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = CursorShape.Card,
    color = CursorColors.SurfaceSoft,
    border = BorderStroke(1.dp, CursorColors.Stroke.copy(alpha = 0.45f)),
  ) {
    androidx.compose.foundation.layout.Column(
      Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("You", style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted)
        Spacer(Modifier.weight(1f))
        Text(message.timestamp, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
      }
      Text(message.body, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
    }
  }
}

@Composable
private fun AssistantMessage(message: ConversationMessage, modifier: Modifier = Modifier) {
  androidx.compose.foundation.layout.Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Xs)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Cursor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
      Spacer(Modifier.size(CursorSpacing.Md))
      Text(message.timestamp, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
    }
    Text(message.body, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Ink)
  }
}

@Composable
private fun Avatar(author: MessageAuthor) {
  val isCursor = author == MessageAuthor.Cursor
  Surface(
    modifier =
      Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(CursorColors.Ink),
    shape = CircleShape,
    color = CursorColors.Ink,
  ) {
    Box(contentAlignment = Alignment.Center) {
      if (isCursor) {
        Image(
          painter = painterResource(R.drawable.cursor_cube_25d),
          contentDescription = null,
          modifier = Modifier.size(15.dp),
          contentScale = ContentScale.Fit,
        )
      } else {
        Text(
          text = "You",
          color = Color.White,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun QuickActionChip(text: String, icon: ImageVector) {
  Surface(
    shape = CursorShape.Chip,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = if (text.isEmpty()) 9.dp else 12.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
    ) {
      androidx.compose.material3.Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = CursorColors.Ink)
      if (text.isNotEmpty()) {
        Text(text, style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}
