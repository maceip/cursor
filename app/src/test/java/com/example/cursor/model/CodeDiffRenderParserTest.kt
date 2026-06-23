package com.example.cursor.model

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class CodeDiffRenderParserTest {
  @Test
  fun fromDeltas_createsStableVisualRows() {
    val model =
      CodeDiffRenderParser.fromDeltas(
        listOf(
          DiffDelta(
            filePath = "src/Timer.kt",
            lineStart = 40,
            hunks = listOf("@@ hook @@", "- oldValue()", "+ newValue()", "  keepValue()"),
          )
        )
      )

    val file = model.files.single()
    assertEquals("src/Timer.kt", file.filePath)
    assertEquals(DiffLineKind.Header, file.lines[0].kind)
    assertEquals(DiffLineKind.Deletion, file.lines[1].kind)
    assertEquals(DiffLineKind.Addition, file.lines[2].kind)
    assertEquals(DiffLineKind.Context, file.lines[3].kind)
    assertEquals("newValue()", file.lines[2].text)
  }

  @Test
  fun fromDeltas_usesUnifiedHunkHeadersForOldAndNewLineNumbers() {
    val model =
      CodeDiffRenderParser.fromDeltas(
        listOf(
          DiffDelta(
            filePath = "src/hooks/useTimer.ts",
            lineStart = 1,
            hunks =
              listOf(
                "@@ -42,2 +42,3 @@",
                " const start = () => {",
                "-  setRunning(true)",
                "+  clearInterval(intervalRef.current)",
                "+  setRunning(true)",
              ),
          )
        )
      )

    val file = model.files.single()
    val lines = file.lines

    assertEquals(2, file.addedLineCount)
    assertEquals(1, file.removedLineCount)
    assertEquals(42, lines[1].oldNumber)
    assertEquals(42, lines[1].newNumber)
    assertEquals(43, lines[2].oldNumber)
    assertNull(lines[2].newNumber)
    assertNull(lines[3].oldNumber)
    assertEquals(43, lines[3].newNumber)
  }
}
