package com.example.cursor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cursor.model.HandoffWorkbench
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorShape
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun HandoffCard(
  handoff: HandoffWorkbench,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    WorkbenchCard(title = "Sent to Cursor Desktop", icon = Icons.Outlined.Check) {
      Text("Continue on your laptop to review and send.", style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted)
      HandoffIllustration()
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CursorShape.CardSmall,
        color = CursorColors.Ink,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(handoff.primaryAction, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
          Spacer(Modifier.weight(1f))
          Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(handoff.sentAt, style = MaterialTheme.typography.labelMedium, color = CursorColors.Muted)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.Refresh, contentDescription = null, tint = CursorColors.Ink, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(CursorSpacing.Xs))
        Text("Resend", style = MaterialTheme.typography.labelMedium, color = CursorColors.Ink)
      }
    }
    WorkbenchCard(title = "Summary", icon = Icons.Outlined.Check) {
      HandoffTimeline(handoff)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CursorShape.CardSmall,
        color = CursorColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CursorColors.Stroke),
      ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("View in Cursor Desktop", style = MaterialTheme.typography.bodyMedium)
          Spacer(Modifier.weight(1f))
          Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(17.dp))
        }
      }
    }
  }
}

@Composable
private fun HandoffIllustration() {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = CursorSpacing.Sm),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    DeviceGlyph(Icons.Outlined.PhoneAndroid)
    Box(
      modifier =
        Modifier
          .padding(horizontal = CursorSpacing.Lg)
          .width(64.dp)
          .height(3.dp)
          .background(CursorColors.Rust, RoundedCornerShape(999.dp)),
    )
    DeviceGlyph(Icons.Outlined.Computer)
  }
}

@Composable
private fun DeviceGlyph(icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Box(
    modifier =
      Modifier
        .size(86.dp)
        .background(CursorColors.SurfaceSoft, RoundedCornerShape(18.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(52.dp), tint = CursorColors.Muted.copy(alpha = 0.72f))
  }
}

@Composable
private fun HandoffTimeline(handoff: HandoffWorkbench) {
  Column(verticalArrangement = Arrangement.spacedBy(CursorSpacing.Md)) {
    handoff.items.forEachIndexed { index, item ->
      Row(verticalAlignment = Alignment.Top) {
        Box(
          modifier =
            Modifier
              .size(22.dp)
              .background(if (item.completed) CursorColors.Ink else CursorColors.RustSoft, androidx.compose.foundation.shape.CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          if (item.completed) {
            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
          } else {
            Box(Modifier.size(9.dp).background(CursorColors.Rust, androidx.compose.foundation.shape.CircleShape))
          }
        }
        Spacer(Modifier.width(CursorSpacing.Md))
        val lines = item.text.split("\n")
        Column(Modifier.weight(1f)) {
          Text(lines.first(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
          lines.drop(1).forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = CursorColors.Muted) }
        }
      }
      if (index != handoff.items.lastIndex) {
        Spacer(Modifier.height(1.dp))
      }
    }
  }
}
