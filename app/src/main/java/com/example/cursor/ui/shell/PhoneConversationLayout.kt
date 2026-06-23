package com.example.cursor.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.model.CursorThreadState
import com.example.cursor.model.WorkbenchState
import com.example.cursor.ui.components.ChatPane
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.components.CursorHeader
import com.example.cursor.ui.components.WorkbenchCards
import com.example.cursor.ui.theme.CursorColors
import com.example.cursor.ui.theme.CursorSpacing

@Composable
fun ConversationPane(
  thread: CursorThreadState,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item { CursorHeader(thread) }
    item { ChatPane(thread) }
  }
}

@Composable
fun PhoneConversationLayout(
  thread: CursorThreadState,
  workbench: WorkbenchState?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .fillMaxSize()
      .background(CursorColors.Cream)
      .safeDrawingPadding()
      .padding(horizontal = CursorSpacing.Lg),
  ) {
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
    ) {
      item { CursorHeader(thread) }
      item {
        ChatPane(thread) {
          if (workbench != null) {
            WorkbenchCards(workbench, Modifier.fillMaxWidth())
          } else {
            Text("Choose a workbench to continue.")
          }
        }
      }
    }
    ComposerDock(
      state = thread.composer,
      modifier = Modifier.navigationBarsPadding().padding(vertical = CursorSpacing.Md),
    )
  }
}

@Composable
fun WorkbenchPane(
  state: WorkbenchState,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(CursorSpacing.Lg),
  ) {
    item { WorkbenchCards(state, Modifier.fillMaxWidth()) }
  }
}
