package com.example.cursor.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cursor.data.FakeFabricRepository
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.feedback.rememberCursorHaptics
import com.example.cursor.ui.motion.CursorBackMorphProvider
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.shell.ConversationPane
import com.example.cursor.ui.shell.CursorShellViewModel
import com.example.cursor.ui.shell.WorkbenchPane
import com.example.cursor.ui.theme.CursorSpacing
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun CursorNavDisplay(
  modifier: Modifier = Modifier,
  viewModel: CursorShellViewModel = viewModel { CursorShellViewModel() },
) {
  val conversation by viewModel.conversation.collectAsStateWithLifecycle()
  val workbench by viewModel.activeWorkbench.collectAsStateWithLifecycle()
  val navigationRequest by viewModel.navigationRequests.collectAsStateWithLifecycle()
  val backStack =
    rememberNavBackStack(
      ConversationKey(FakeFabricRepository.DefaultThreadId),
      WorkbenchKey(FakeFabricRepository.DefaultThreadId, WorkbenchKind.Spec),
    )
  var backMorphProgress by androidx.compose.runtime.remember { mutableFloatStateOf(0f) }
  val haptics = rememberCursorHaptics()

  LaunchedEffect(Unit) {
    if (backStack.none { it is WorkbenchKey }) {
      backStack.add(WorkbenchKey(FakeFabricRepository.DefaultThreadId, WorkbenchKind.Spec))
    }
  }

  LaunchedEffect(navigationRequest, conversation.threadId) {
    val kind = navigationRequest ?: return@LaunchedEffect
    backStack.removeAll { it is WorkbenchKey && it.threadId == conversation.threadId }
    backStack.add(WorkbenchKey(conversation.threadId, kind))
    viewModel.navigationHandled()
  }

  PredictiveBackHandler(enabled = backStack.size > 1) { progress ->
    var gestureStarted = false
    try {
      progress.collect { event ->
        if (!gestureStarted) {
          haptics.predictiveBackStart()
          gestureStarted = true
        }
        backMorphProgress = event.progress
      }
      haptics.predictiveBackCommit()
      backStack.removeLastOrNull()
    } catch (cancellation: CancellationException) {
      if (gestureStarted) haptics.predictiveBackCancel()
      throw cancellation
    } finally {
      backMorphProgress = 0f
    }
  }

  BoxWithConstraints(modifier) {
    val canShowTwoPane = maxWidth >= 600.dp
    val openWorkbench: (WorkbenchKind) -> Unit = viewModel::openWorkbench
    val sceneStrategy =
      CursorFoldableSceneStrategy(
        canShowTwoPane = canShowTwoPane,
        conversationContent = {
          ConversationPane(
            conversation = conversation,
            onWorkbenchSelected = openWorkbench,
          )
        },
        workbenchContent = {
          WorkbenchPane(
            workbench = workbench,
            onWorkbenchSelected = openWorkbench,
          )
        },
        composerContent = {
          ComposerDock(
            composer = conversation.composer,
            modifier = Modifier.navigationBarsPadding().padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
          )
        },
      )

    CursorBackMorphProvider(progress = backMorphProgress) {
      NavDisplay(
        backStack = backStack,
        onBack = {
          haptics.predictiveBackCommit()
          backStack.removeLastOrNull()
        },
        sceneStrategy = sceneStrategy,
        sizeTransform = cursorSceneSizeTransform(),
        transitionSpec = { cursorSceneTransition() },
        popTransitionSpec = { cursorPopSceneTransition() },
        predictivePopTransitionSpec = { progress -> cursorPredictivePopSceneTransition(progress) },
        entryProvider =
          cursorEntryProvider(
            conversation = conversation,
            workbench = workbench,
            onWorkbenchSelected = openWorkbench,
          ),
      )
    }
  }
}
