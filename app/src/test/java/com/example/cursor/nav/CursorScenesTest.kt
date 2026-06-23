package com.example.cursor.nav

import com.example.cursor.model.WorkbenchKind
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CursorScenesTest {
  @Test
  fun compatiblePanePair_requiresConversationWorkbenchSameThread() {
    assertTrue(
      compatiblePanePair(
        previousRole = PaneRole.Conversation,
        previousThreadId = "thread-1",
        latestRole = PaneRole.Workbench,
        latestThreadId = "thread-1",
        latestKind = WorkbenchKind.Spec,
      )
    )
  }

  @Test
  fun compatiblePanePair_rejectsDifferentThreads() {
    assertFalse(
      compatiblePanePair(
        previousRole = PaneRole.Conversation,
        previousThreadId = "thread-1",
        latestRole = PaneRole.Workbench,
        latestThreadId = "thread-2",
        latestKind = WorkbenchKind.Spec,
      )
    )
  }
}
