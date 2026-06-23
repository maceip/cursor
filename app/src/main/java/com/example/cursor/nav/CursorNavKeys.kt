package com.example.cursor.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ConversationKey(val threadId: String) : NavKey

@Serializable
data class WorkbenchKey(
  val threadId: String,
  val kind: WorkbenchKind,
) : NavKey

@Serializable
enum class WorkbenchKind {
  Spec,
  CodeReview,
  Handoff,
  Artifact,
  Writing,
}
