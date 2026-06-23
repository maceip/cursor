package com.example.cursor.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ComposerState
import com.example.cursor.ui.motion.cursorBackMorph
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorElevation
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ComposerDock(
  composer: ComposerState,
  modifier: Modifier = Modifier,
) {
  val initialAttachments =
    remember(composer.attachments) {
      composer.attachments.mapIndexed { index, attachment ->
        CursorPromptAttachment(
          id = "composer-$index-${attachment.label}",
          displayName = attachment.label,
          detail = attachment.detail,
          kind = CursorAttachmentKind.File,
        )
      }
    }
  val promptState = rememberCursorPromptBarState(initialAttachments = initialAttachments)
  val sendScale by animateFloatAsState(
    targetValue = if (promptState.sendState == CursorSendState.Ready) 1f else 0.96f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
    label = "CursorSendScale",
  )

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .cursorBackMorph(scaleXTarget = 0.92f, scaleYTarget = 0.68f, alphaTarget = 0.74f, translateY = 24f),
    shape = CursorShape.Dock,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
    tonalElevation = CursorElevation.Raised,
  ) {
    Column(
      Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
    ) {
      if (promptState.attachments.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
          items(promptState.attachments, key = { it.id }) { attachment ->
            CursorAttachmentChip(attachment = attachment, onRemove = { promptState.removeAttachment(attachment.id) })
          }
        }
      }
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp, max = 92.dp),
      ) {
        BasicTextField(
          value = promptState.fieldValue,
          onValueChange = { promptState.fieldValue = it },
          modifier = Modifier.fillMaxWidth(),
          textStyle = LocalTextStyle.current.copy(
            color = CursorColors.Ink,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
          ),
          cursorBrush = SolidColor(CursorColors.Ink),
          decorationBox = { innerTextField ->
            if (promptState.text.isEmpty()) {
              Text(composer.promptHint, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Faint)
            }
            innerTextField()
          },
        )
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        CursorIconButton(Icons.Outlined.Add)
        Spacer(Modifier.size(CursorSpacing.Sm))
        CursorIconButton(Icons.Outlined.AttachFile)
        Spacer(Modifier.weight(1f))
        CursorIconButton(Icons.Outlined.Mic)
        Spacer(Modifier.size(CursorSpacing.Sm))
        CursorIconButton(
          Icons.Outlined.ArrowUpward,
          modifier = Modifier.scale(sendScale),
          dark = true,
          onClick = { if (promptState.sendState == CursorSendState.Ready) promptState.reset() },
        )
      }
    }
  }
}

@Composable
private fun CursorAttachmentChip(
  attachment: CursorPromptAttachment,
  onRemove: () -> Unit,
) {
  Surface(
    shape = CursorShape.CardSmall,
    color = CursorColors.SurfaceSoft,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Row(
      modifier = Modifier.padding(start = 7.dp, end = 7.dp, top = 5.dp, bottom = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm),
    ) {
      androidx.compose.material3.Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = CursorColors.Ink)
      Column {
        Text(attachment.displayName, style = MaterialTheme.typography.labelMedium)
        attachment.detail?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted) }
      }
      Surface(shape = androidx.compose.foundation.shape.CircleShape, color = CursorColors.Surface, onClick = onRemove) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
          Text("x", style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
        }
      }
    }
  }
}
