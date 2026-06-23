package com.example.cursor.nav

import androidx.navigation3.runtime.NavKey
import com.example.cursor.model.WorkbenchKind
import kotlinx.serialization.Serializable

@Serializable
data class ConversationKey(
  val threadId: String,
) : NavKey

@Serializable
data class WorkbenchKey(
  val threadId: String,
  val kind: WorkbenchKind,
) : NavKey
