package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.R
import com.example.cursor.model.ConversationState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CursorHeader(
  conversation: ConversationState,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      CursorMark()
      Spacer(Modifier.width(CursorSpacing.Xs))
      Text(
        text = "CURSOR",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.weight(1f))
      HeaderIconButton(Icons.Outlined.AccessTime)
      Spacer(Modifier.width(CursorSpacing.Xs))
      HeaderIconButton(Icons.Outlined.MoreVert)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm), verticalAlignment = Alignment.CenterVertically) {
      HeaderChip(conversation.workspaceName)
      Spacer(Modifier.weight(1f))
      HeaderChip(conversation.modelName)
    }
  }
}

@Composable
private fun CursorMark(modifier: Modifier = Modifier) {
  Image(
    painter = painterResource(R.drawable.cursor_cube_25d),
    contentDescription = null,
    modifier = modifier.size(16.dp),
    contentScale = ContentScale.Fit,
  )
}

@Composable
private fun HeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Surface(
    shape = androidx.compose.foundation.shape.CircleShape,
    color = Color.Transparent,
    onClick = {},
  ) {
    Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = CursorColors.Ink)
    }
  }
}

@Composable
private fun HeaderChip(text: String) {
  Surface(
    shape = CursorShape.Chip,
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Xs),
    ) {
      Text(text, style = MaterialTheme.typography.labelMedium)
      Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = CursorColors.Ink)
    }
  }
}
