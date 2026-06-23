package com.example.cursor.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.cursor.data.fabric.FakeFabricFixtures
import com.example.cursor.nav.WorkbenchKind
import com.example.cursor.ui.theme.CursorClaudeTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 820)
@Composable
fun PhoneSpecPreview() = PhonePreview(WorkbenchKind.Spec)

@Preview(showBackground = true, widthDp = 360, heightDp = 820)
@Composable
fun PhoneCodeReviewPreview() = PhonePreview(WorkbenchKind.CodeReview)

@Preview(showBackground = true, widthDp = 360, heightDp = 820)
@Composable
fun PhoneHandoffPreview() = PhonePreview(WorkbenchKind.Handoff)

@Preview(showBackground = true, widthDp = 360, heightDp = 820)
@Composable
fun PhoneArtifactPreview() = PhonePreview(WorkbenchKind.Artifact)

@Preview(showBackground = true, widthDp = 360, heightDp = 820)
@Composable
fun PhoneWritingPreview() = PhonePreview(WorkbenchKind.Writing)

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
fun FoldableSpecPreview() = FoldablePreview(WorkbenchKind.Spec)

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
fun FoldableCodeReviewPreview() = FoldablePreview(WorkbenchKind.CodeReview)

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
fun FoldableHandoffPreview() = FoldablePreview(WorkbenchKind.Handoff)

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
fun FoldableArtifactPreview() = FoldablePreview(WorkbenchKind.Artifact)

@Preview(showBackground = true, widthDp = 840, heightDp = 720)
@Composable
fun FoldableWritingPreview() = FoldablePreview(WorkbenchKind.Writing)

@Composable
private fun PhonePreview(kind: WorkbenchKind) {
  val state = remember { FakeFabricFixtures.workspaceState() }
  CursorClaudeTheme {
    PhoneConversationLayout(
      thread = state.thread,
      workbench = state.workbenches.getValue(kind),
    )
  }
}

@Composable
private fun FoldablePreview(kind: WorkbenchKind) {
  val state = remember { FakeFabricFixtures.workspaceState() }
  CursorClaudeTheme {
    FoldableWorkbenchLayout(
      thread = state.thread,
      workbench = state.workbenches.getValue(kind),
    )
  }
}
