package com.example.cursor.model

import kotlinx.serialization.Serializable

@Serializable
enum class WorkbenchKind {
  Spec,
  CodeReview,
  Handoff,
  Artifact,
  Writing,
}
