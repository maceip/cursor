package com.example.cursor.diff

object DiffDeltaParser {
  fun parse(
    filePath: String,
    lineStart: Int,
    hunkLines: List<String>,
  ): FileDiffRenderModel {
    val hunks = mutableListOf<DiffHunkRenderModel>()
    var activeHeader = "@@ line $lineStart @@"
    var activeLines = mutableListOf<DiffLineRenderToken>()
    var oldLine = lineStart
    var newLine = lineStart

    fun flushHunk() {
      if (activeLines.isNotEmpty()) {
        hunks += DiffHunkRenderModel(activeHeader, activeLines)
        activeLines = mutableListOf()
      }
    }

    hunkLines.forEach { rawLine ->
      if (rawLine.startsWith("@@")) {
        flushHunk()
        activeHeader = rawLine
        parseHeaderStart(rawLine)?.let { (oldStart, newStart) ->
          oldLine = oldStart
          newLine = newStart
        }
        return@forEach
      }

      val kind =
        when {
          rawLine.startsWith("+") -> DiffLineKind.Added
          rawLine.startsWith("-") -> DiffLineKind.Removed
          else -> DiffLineKind.Context
        }
      val text = rawLine.removePrefix("+").removePrefix("-").removePrefix(" ")
      val token =
        when (kind) {
          DiffLineKind.Added -> DiffLineRenderToken(kind, oldLineNumber = null, newLineNumber = newLine++, text = text)
          DiffLineKind.Removed -> DiffLineRenderToken(kind, oldLineNumber = oldLine++, newLineNumber = null, text = text)
          DiffLineKind.Context -> DiffLineRenderToken(kind, oldLineNumber = oldLine++, newLineNumber = newLine++, text = text)
        }
      activeLines += token
    }
    flushHunk()

    return FileDiffRenderModel(filePath = filePath, hunks = hunks)
  }

  private fun parseHeaderStart(header: String): Pair<Int, Int>? {
    val parts = header.split(" ")
    val oldPart = parts.firstOrNull { it.startsWith("-") } ?: return null
    val newPart = parts.firstOrNull { it.startsWith("+") } ?: return null
    val oldStart = oldPart.removePrefix("-").substringBefore(",").toIntOrNull() ?: return null
    val newStart = newPart.removePrefix("+").substringBefore(",").toIntOrNull() ?: return null
    return oldStart to newStart
  }
}
