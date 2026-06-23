package com.example.cursor.nav

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class CursorScenesTest {
  @Test
  fun findCompatiblePair_pairsConversationAndWorkbenchForSameThread() {
    val entries =
      listOf(
        conversationEntry(ConversationKey("thread-1")) {},
        workbenchEntry(WorkbenchKey("thread-1", WorkbenchKind.CodeReview)) {},
      )

    val pair = findCompatiblePair(entries)

    assertEquals("thread-1", pair?.threadId)
    assertEquals(0, pair?.conversationEntryIndex)
    assertEquals(1, pair?.workbenchEntryIndex)
    assertEquals(WorkbenchKind.CodeReview, pair?.workbenchKind)
  }

  @Test
  fun findCompatiblePair_rejectsDifferentThreads() {
    val entries =
      listOf(
        conversationEntry(ConversationKey("thread-1")) {},
        workbenchEntry(WorkbenchKey("thread-2", WorkbenchKind.CodeReview)) {},
      )

    assertNull(findCompatiblePair(entries))
  }

}
