package com.example.cursor.model

import junit.framework.TestCase.assertEquals
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
}
