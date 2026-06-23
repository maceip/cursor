package com.example.cursor.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.cursor.data.InMemoryFabricRepository
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.theme.CursorClaudeTheme
import com.example.cursor.ui.theme.CursorSpacing

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ScreenshotPhoneSpec() {
  PhoneScreenshotPreview(WorkbenchKind.Spec)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ScreenshotPhoneCodeReview() {
  PhoneScreenshotPreview(WorkbenchKind.CodeReview)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ScreenshotPhoneHandoff() {
  PhoneScreenshotPreview(WorkbenchKind.Handoff)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ScreenshotPhoneArtifact() {
  PhoneScreenshotPreview(WorkbenchKind.Artifact)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 820)
@Composable
fun ScreenshotPhoneWriting() {
  PhoneScreenshotPreview(WorkbenchKind.Writing)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
fun ScreenshotFoldSpec() {
  FoldScreenshotPreview(WorkbenchKind.Spec)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
fun ScreenshotFoldCodeReview() {
  FoldScreenshotPreview(WorkbenchKind.CodeReview)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
fun ScreenshotFoldHandoff() {
  FoldScreenshotPreview(WorkbenchKind.Handoff)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
fun ScreenshotFoldArtifact() {
  FoldScreenshotPreview(WorkbenchKind.Artifact)
}

@PreviewTest
@Preview(showBackground = true, widthDp = 900, heightDp = 640)
@Composable
fun ScreenshotFoldWriting() {
  FoldScreenshotPreview(WorkbenchKind.Writing)
}

@Composable
private fun PhoneScreenshotPreview(kind: WorkbenchKind) {
  val repository = remember { InMemoryFabricRepository(initialKind = kind) }
  val conversation by repository.conversation.collectAsState()
  val workbench by repository.activeWorkbench.collectAsState()
  val topology by repository.topology.collectAsState()

  CursorClaudeTheme {
    PhoneConversationLayout(
      conversation = conversation,
      workbench = workbench,
      topology = topology,
      onWorkbenchSelected = {},
      onMessageSubmitted = {},
    )
  }
}

@Composable
private fun FoldScreenshotPreview(kind: WorkbenchKind) {
  val repository = remember { InMemoryFabricRepository(initialKind = kind) }
  val conversation by repository.conversation.collectAsState()
  val workbench by repository.activeWorkbench.collectAsState()
  val topology by repository.topology.collectAsState()

  CursorClaudeTheme {
    CursorTwoPaneLayout(
      conversation = { ConversationPane(conversation, onWorkbenchSelected = {}) },
      workbench = {
        WorkbenchPane(
          workbench = workbench,
          topology = topology,
          onWorkbenchSelected = {},
        )
      },
      composer = {
        ComposerDock(
          composer = conversation.composer,
          onSubmit = {},
          modifier = Modifier.padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
        )
      },
    )
  }
}
