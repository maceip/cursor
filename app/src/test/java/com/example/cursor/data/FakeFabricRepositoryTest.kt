package com.example.cursor.data

import com.example.cursor.model.WorkbenchKind
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FakeFabricRepositoryTest {
  @Test
  fun startsWithProductionShapedConversationAndTopology() {
    val repository = FakeFabricRepository()

    assertEquals(FakeFabricRepository.DefaultThreadId, repository.conversation.value.threadId)
    assertEquals(WorkbenchKind.Spec, repository.activeWorkbench.value.kind)
    assertTrue(repository.topology.value.hosts.isNotEmpty())
    assertTrue(repository.topology.value.latestSequenceNumber > 0)
  }

  @Test
  fun openWorkbench_switchesActiveWorkbenchShape() {
    val repository = FakeFabricRepository()

    repository.openWorkbench(WorkbenchKind.CodeReview)

    assertEquals(WorkbenchKind.CodeReview, repository.activeWorkbench.value.kind)
    assertTrue(repository.activeWorkbench.value.codeReview?.diff?.files?.isNotEmpty() == true)
  }
}
