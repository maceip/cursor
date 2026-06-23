package com.example.cursor.model

data class WorkbenchState(
  val threadId: String,
  val kind: WorkbenchKind,
  val status: String,
  val title: String,
  val summary: String,
  val spec: SpecWorkbench?,
  val codeReview: CodeReviewWorkbench?,
  val handoff: HandoffWorkbench?,
  val artifact: ArtifactWorkbench?,
  val draft: DraftWorkbench?,
)

data class SpecWorkbench(
  val sections: List<SpecSection>,
  val nextSteps: List<ChecklistItem>,
)

data class SpecSection(
  val title: String,
  val body: String,
)

data class ChecklistItem(
  val text: String,
  val completed: Boolean,
)

data class CodeReviewWorkbench(
  val plan: List<ChecklistItem>,
  val diff: CodeDiffRenderModel,
  val files: List<String>,
)

data class HandoffWorkbench(
  val targetDevice: String,
  val items: List<ChecklistItem>,
)

data class ArtifactWorkbench(
  val name: String,
  val subtitle: String,
  val progress: Float,
  val previewLines: List<String>,
)

data class DraftWorkbench(
  val selectedTone: String,
  val tones: List<String>,
  val subject: String,
  val body: String,
  val rationale: String,
)
