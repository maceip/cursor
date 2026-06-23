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
)

data class DiffLineRenderModel(
  val number: Int?,
  val marker: String,
  val text: String,
  val kind: DiffLineKind,
)

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
          var currentLine = delta.lineStart
          val lines =
            delta.hunks.map { rawLine ->
              val kind =
                when {
                  rawLine.startsWith("@@") -> DiffLineKind.Header
                  rawLine.startsWith("+") -> DiffLineKind.Addition
                  rawLine.startsWith("-") -> DiffLineKind.Deletion
                  else -> DiffLineKind.Context
                }
              val number =
                when (kind) {
                  DiffLineKind.Header -> null
                  DiffLineKind.Deletion -> currentLine
                  DiffLineKind.Addition,
                  DiffLineKind.Context -> currentLine++
                }
              DiffLineRenderModel(
                number = number,
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
}
