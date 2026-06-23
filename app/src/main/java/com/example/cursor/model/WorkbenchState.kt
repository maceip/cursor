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
  val sources: List<SourceCard>,
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
  val readyTitle: String,
  val readyBody: String,
)

data class HandoffWorkbench(
  val targetDevice: String,
  val items: List<ChecklistItem>,
  val primaryAction: String,
  val sentAt: String,
)

data class ArtifactWorkbench(
  val name: String,
  val subtitle: String,
  val progress: Float,
  val questionLabel: String,
  val category: String,
  val question: String,
  val choices: List<ArtifactChoice>,
  val footerStart: String,
  val footerEnd: String,
)

data class DraftWorkbench(
  val selectedTone: String,
  val tones: List<String>,
  val subject: String,
  val body: String,
  val rationale: String,
)

data class SourceCard(
  val title: String,
  val detail: String,
)

data class ArtifactChoice(
  val label: String,
  val text: String,
  val selected: Boolean,
)
