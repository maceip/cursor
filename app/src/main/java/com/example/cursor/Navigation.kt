package com.example.cursor

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.WindowLayoutInfo
import com.example.cursor.nav.CursorNavDisplay
import com.example.cursor.ui.control.CursorAppViewModel
import com.example.cursor.ui.control.CursorOnboardingScreen
import com.example.cursor.ui.control.CursorPoolHome
import com.example.cursor.ui.shell.CursorShellViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainNavigation(windowLayoutInfo: StateFlow<WindowLayoutInfo>) {
  val context = LocalContext.current.applicationContext
  val appViewModel: CursorAppViewModel = viewModel { CursorAppViewModel(context) }
  val shellViewModel: CursorShellViewModel =
    viewModel(key = "cursor-shell") { CursorShellViewModel.roomBacked(context, appViewModel.fabricStreamClient) }
  val state by appViewModel.state.collectAsStateWithLifecycle()

  if (!state.anyLinked) {
    CursorOnboardingScreen(
      state = state,
      onLinkKey = appViewModel::linkKey,
      modifier = Modifier.safeDrawingPadding(),
    )
  } else {
    CursorPoolHome(
      state = state,
      onRefresh = appViewModel::refresh,
      onLinkKey = appViewModel::linkKey,
      onUnlink = appViewModel::unlink,
      onSelectAgent = appViewModel::selectAgent,
      onSelectRun = appViewModel::selectRun,
      onCreateAgent = appViewModel::createAgent,
      onArchiveAgent = appViewModel::archiveAgent,
      onUnarchiveAgent = appViewModel::unarchiveAgent,
      onHydrateArtifactDownload = appViewModel::hydrateArtifactDownload,
      console = {
        CursorNavDisplay(
          windowLayoutInfo = windowLayoutInfo,
          modifier = Modifier.safeDrawingPadding(),
          providedViewModel = shellViewModel,
        )
      },
    )
  }
}
