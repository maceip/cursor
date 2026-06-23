package com.example.cursor.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.CursorThreadState
import com.example.cursor.model.WorkbenchState
import com.example.cursor.nav.CursorTwoPaneSceneContent
import com.example.cursor.ui.components.ComposerDock

@Composable
fun FoldableWorkbenchLayout(
  thread: CursorThreadState,
  workbench: WorkbenchState,
  modifier: Modifier = Modifier,
) {
  CursorTwoPaneSceneContent(
    conversation = { ConversationPane(thread) },
    workbench = { WorkbenchPane(workbench) },
    composer = { ComposerDock(thread.composer) },
    modifier = modifier,
  )
}
