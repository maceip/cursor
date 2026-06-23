package com.example.cursor.model

import com.example.cursor.diff.FileDiffRenderModel
import com.example.cursor.nav.WorkbenchKind

data class CursorWorkspaceState(
  val thread: CursorThreadState,
  val workbenches: Map<WorkbenchKind, WorkbenchState>,
)

data class WorkbenchState(
  val threadId: String,
  val kind: WorkbenchKind,
  val title: String,
  val summary: String,
  val statusLabel: String,
  val spec: SpecCardState? = null,
  val diff: CodeDiffCardState? = null,
  val handoff: HandoffCardState? = null,
  val artifact: ArtifactPreviewState? = null,
  val draft: DraftCardState? = null,
)

data class SpecCardState(
  val title: String,
  val bullets: List<SpecBullet>,
  val nextSteps: List<TaskItem>,
)

data class SpecBullet(
  val label: String,
  val body: String,
)

data class TaskItem(
  val label: String,
  val detail: String? = null,
  val complete: Boolean = false,
)

data class CodeDiffCardState(
  val title: String,
  val files: List<FileDiffRenderModel>,
  val touchedFiles: List<FileReference>,
)

data class FileReference(
  val name: String,
  val path: String,
)

data class HandoffCardState(
  val targetDevice: String,
  val message: String,
  val tasks: List<TaskItem>,
)

data class ArtifactPreviewState(
  val appName: String,
  val subtitle: String,
  val progressLabel: String,
  val question: String,
  val options: List<QuizOption>,
)

data class QuizOption(
  val label: String,
  val text: String,
  val selected: Boolean = false,
)

data class DraftCardState(
  val selectedTone: String,
  val tones: List<String>,
  val subject: String,
  val body: String,
  val rationale: String,
)
