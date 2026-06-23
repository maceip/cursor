package com.example.cursor.diff

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class DiffDeltaParserTest {
  @Test
  fun parse_createsImmutableLineTokens() {
    val model =
      DiffDeltaParser.parse(
        filePath = "src/hooks/useTimer.ts",
        lineStart = 42,
        hunkLines =
          listOf(
            "@@ -42,2 +42,3 @@",
            " const start = () => {",
            "-  setRunning(true)",
            "+  clearInterval(intervalRef.current)",
            "+  setRunning(true)",
          ),
      )

    val lines = model.hunks.single().lines
    assertEquals("src/hooks/useTimer.ts", model.filePath)
    assertEquals(2, model.addedLineCount)
    assertEquals(1, model.removedLineCount)
    assertEquals(DiffLineKind.Context, lines[0].kind)
    assertEquals(42, lines[0].oldLineNumber)
    assertEquals(42, lines[0].newLineNumber)
    assertEquals(DiffLineKind.Removed, lines[1].kind)
    assertEquals(43, lines[1].oldLineNumber)
    assertNull(lines[1].newLineNumber)
    assertEquals(DiffLineKind.Added, lines[2].kind)
    assertNull(lines[2].oldLineNumber)
    assertEquals(43, lines[2].newLineNumber)
  }
}
