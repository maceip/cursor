package com.example.cursor.diff

data class FileDiffRenderModel(
  val filePath: String,
  val hunks: List<DiffHunkRenderModel>,
) {
  val addedLineCount: Int = hunks.sumOf { hunk -> hunk.lines.count { it.kind == DiffLineKind.Added } }
  val removedLineCount: Int = hunks.sumOf { hunk -> hunk.lines.count { it.kind == DiffLineKind.Removed } }
}

data class DiffHunkRenderModel(
  val header: String,
  val lines: List<DiffLineRenderToken>,
)

data class DiffLineRenderToken(
  val kind: DiffLineKind,
  val oldLineNumber: Int?,
  val newLineNumber: Int?,
  val text: String,
)

enum class DiffLineKind {
  Context,
  Added,
  Removed,
}
