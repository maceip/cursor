package com.example.cursor.nav

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import com.example.cursor.data.FabricDefaults
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.ui.components.ComposerDock
import com.example.cursor.ui.shell.ConversationPane
import com.example.cursor.ui.shell.CursorShellViewModel
import com.example.cursor.ui.shell.WorkbenchPane
import com.example.cursor.ui.theme.CursorSpacing
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CursorNavDisplay(
  windowLayoutInfo: StateFlow<WindowLayoutInfo>,
  modifier: Modifier = Modifier,
  providedViewModel: CursorShellViewModel? = null,
) {
  val context = LocalContext.current.applicationContext
  val viewModel: CursorShellViewModel = providedViewModel ?: viewModel { CursorShellViewModel.roomBacked(context) }
  val lifecycleOwner = LocalLifecycleOwner.current
  val conversation by viewModel.conversation.collectAsStateWithLifecycle()
  val workbench by viewModel.activeWorkbench.collectAsStateWithLifecycle()
  val topology by viewModel.topology.collectAsStateWithLifecycle()
  val navigationRequest by viewModel.navigationRequests.collectAsStateWithLifecycle()
  val layoutInfo by windowLayoutInfo.collectAsStateWithLifecycle()
  val backStack = rememberNavBackStack(ConversationKey(FabricDefaults.DefaultThreadId))

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer =
      LifecycleEventObserver { _, event ->
        when (event) {
          Lifecycle.Event.ON_START -> viewModel.connect()
          Lifecycle.Event.ON_STOP -> viewModel.disconnect()
          else -> Unit
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    viewModel.connect()
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      viewModel.disconnect()
    }
  }

  LaunchedEffect(Unit) {
    if (backStack.none { it is WorkbenchKey }) {
      backStack.add(WorkbenchKey(FabricDefaults.DefaultThreadId, WorkbenchKind.Spec))
    }
  }

  LaunchedEffect(navigationRequest, conversation.threadId) {
    val kind = navigationRequest ?: return@LaunchedEffect
    backStack.removeAll { it is WorkbenchKey && it.threadId == conversation.threadId }
    backStack.add(WorkbenchKey(conversation.threadId, kind))
    viewModel.navigationHandled()
  }

  BoxWithConstraints(modifier) {
    val hasSeparatingFold =
      layoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .any { feature -> feature.isSeparating }
    val canShowTwoPane = hasSeparatingFold || maxWidth >= 600.dp
    val openWorkbench: (WorkbenchKind) -> Unit = viewModel::openWorkbench
    val submitMessage: (String) -> Unit = viewModel::submitUserMessage
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
            topology = topology,
            onWorkbenchSelected = openWorkbench,
          )
        },
        composerContent = {
          ComposerDock(
            composer = conversation.composer,
            onSubmit = submitMessage,
            modifier = Modifier.navigationBarsPadding().padding(horizontal = CursorSpacing.Xl, vertical = CursorSpacing.Lg),
          )
        },
      )

    NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      sceneStrategy = sceneStrategy,
      entryProvider =
        cursorEntryProvider(
          conversation = conversation,
          workbench = workbench,
          topology = topology,
          onWorkbenchSelected = openWorkbench,
          onMessageSubmitted = submitMessage,
        ),
    )
  }
}
