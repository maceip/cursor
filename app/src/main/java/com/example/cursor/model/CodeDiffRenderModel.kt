package com.example.cursor.model

data class DiffDelta(
  val filePath: String,
  val lineStart: Int,
  val hunks: List<String>,
)

data class CodeDiffRenderModel(
  val files: List<DiffFileRenderModel>,
)

data class DiffFileRenderModel(
  val filePath: String,
  val lineStart: Int,
  val lines: List<DiffLineRenderModel>,
) {
  val addedLineCount: Int = lines.count { it.kind == DiffLineKind.Addition }
  val removedLineCount: Int = lines.count { it.kind == DiffLineKind.Deletion }
}

data class DiffLineRenderModel(
  val oldNumber: Int?,
  val newNumber: Int?,
  val marker: String,
  val text: String,
  val kind: DiffLineKind,
) {
  val number: Int? = newNumber ?: oldNumber
}

enum class DiffLineKind {
  Header,
  Context,
  Addition,
  Deletion,
}

object CodeDiffRenderParser {
  fun fromDeltas(deltas: List<DiffDelta>): CodeDiffRenderModel =
    CodeDiffRenderModel(
      files =
        deltas.map { delta ->
          var oldLine = delta.lineStart
          var newLine = delta.lineStart
          val lines =
            delta.hunks.map { rawLine ->
              val kind =
                when {
                  rawLine.startsWith("@@") -> DiffLineKind.Header
                  rawLine.startsWith("+") -> DiffLineKind.Addition
                  rawLine.startsWith("-") -> DiffLineKind.Deletion
                  else -> DiffLineKind.Context
                }
              if (kind == DiffLineKind.Header) {
                parseHeaderStart(rawLine)?.let { (oldStart, newStart) ->
                  oldLine = oldStart
                  newLine = newStart
                }
              }
              val oldNumber =
                when (kind) {
                  DiffLineKind.Header -> null
                  DiffLineKind.Addition -> null
                  DiffLineKind.Deletion -> oldLine++
                  DiffLineKind.Context -> oldLine++
                }
              val newNumber =
                when (kind) {
                  DiffLineKind.Header -> null
                  DiffLineKind.Deletion -> null
                  DiffLineKind.Addition -> newLine++
                  DiffLineKind.Context -> newLine++
                }
              DiffLineRenderModel(
                oldNumber = oldNumber,
                newNumber = newNumber,
                marker = markerFor(rawLine, kind),
                text = textFor(rawLine, kind),
                kind = kind,
              )
            }
          DiffFileRenderModel(delta.filePath, delta.lineStart, lines)
        }
    )

  private fun markerFor(rawLine: String, kind: DiffLineKind): String =
    when (kind) {
      DiffLineKind.Header -> "@@"
      DiffLineKind.Addition -> "+"
      DiffLineKind.Deletion -> "-"
      DiffLineKind.Context -> " "
    }

  private fun textFor(rawLine: String, kind: DiffLineKind): String =
    when {
      kind == DiffLineKind.Header -> rawLine
      rawLine.isEmpty() -> rawLine
      rawLine.first() == '+' || rawLine.first() == '-' || rawLine.first() == ' ' -> rawLine.drop(1).removePrefix(" ")
      else -> rawLine
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
