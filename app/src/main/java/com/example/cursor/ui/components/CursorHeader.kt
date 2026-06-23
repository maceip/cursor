package com.example.cursor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.ConversationState
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun CursorHeader(
  conversation: ConversationState,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      CursorMark()
      Spacer(Modifier.width(CursorSpacing.Sm))
      Text(
        text = "CURSOR",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.weight(1f))
      CursorHeaderButton("History")
      Spacer(Modifier.width(CursorSpacing.Sm))
      CursorHeaderButton("More")
    }
    Row(horizontalArrangement = Arrangement.spacedBy(CursorSpacing.Sm)) {
      CursorChip(conversation.workspaceName)
      CursorChip(conversation.modelName)
    }
  }
}

@Composable
private fun CursorMark(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .size(28.dp)
        .clip(RoundedCornerShape(7.dp))
        .background(Brush.linearGradient(listOf(Color.Black, Color(0xFF686866))))
        .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(7.dp)),
  )
}

@Composable
private fun CursorHeaderButton(text: String) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = CursorColors.Surface,
    border = BorderStroke(1.dp, CursorColors.Stroke),
  ) {
    Text(text, Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium)
  }
}
