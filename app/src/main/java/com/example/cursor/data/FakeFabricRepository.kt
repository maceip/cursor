package com.example.cursor.data

import com.example.cursor.model.AgentRunState
import com.example.cursor.model.AgentStatus
import com.example.cursor.model.ArtifactWorkbench
import com.example.cursor.model.AttachmentChip
import com.example.cursor.model.ChecklistItem
import com.example.cursor.model.CodeDiffRenderParser
import com.example.cursor.model.CodeReviewWorkbench
import com.example.cursor.model.ComposerState
import com.example.cursor.model.ConversationMessage
import com.example.cursor.model.ConversationState
import com.example.cursor.model.DiffDelta
import com.example.cursor.model.DraftWorkbench
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.HandoffWorkbench
import com.example.cursor.model.HostTopology
import com.example.cursor.model.MessageAuthor
import com.example.cursor.model.PromptToken
import com.example.cursor.model.PromptTokenKind
import com.example.cursor.model.SpecSection
import com.example.cursor.model.SpecWorkbench
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchShortcut
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFabricRepository(
  private val threadId: String = DefaultThreadId,
) : FabricRepository {
  private val _conversation = MutableStateFlow(seedConversation(threadId))
  override val conversation: StateFlow<ConversationState> = _conversation.asStateFlow()

  private val _activeWorkbench = MutableStateFlow(seedWorkbench(threadId, WorkbenchKind.Spec))
  override val activeWorkbench: StateFlow<WorkbenchState> = _activeWorkbench.asStateFlow()

  private val _topology = MutableStateFlow(seedTopology())
  override val topology: StateFlow<FabricTopologyState> = _topology.asStateFlow()

  override fun openWorkbench(kind: WorkbenchKind) {
    _activeWorkbench.value = seedWorkbench(threadId, kind)
  }

  companion object {
    const val DefaultThreadId = "thread-cursor-mobile"
  }
}

private fun seedConversation(threadId: String) =
  ConversationState(
    threadId = threadId,
    title = "Cursor mobile control tower",
    workspaceName = "acme/mobile",
    modelName = "Cursor Pro",
    messages =
      listOf(
        ConversationMessage(
          id = "m1",
          author = MessageAuthor.User,
          body = "Help me plan the mobile agent controller, review code changes, and hand off work to desktop.",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "m2",
          author = MessageAuthor.Cursor,
          body = "I set up a shared conversation stack with workbench panes for specs, diffs, artifacts, handoffs, and writing drafts.",
          timestamp = "10:13 AM",
        ),
      ),
    composer =
      ComposerState(
        promptHint = "Ask Cursor, type @ for context, or / for actions",
        attachments =
          listOf(
            AttachmentChip("todo.md", ".shape/design"),
            AttachmentChip("mobile-agent", "workspace"),
          ),
        tokens =
          listOf(
            PromptToken("@workspace", PromptTokenKind.Mention),
            PromptToken("/spec", PromptTokenKind.SlashCommand),
            PromptToken("FabricRepository.kt", PromptTokenKind.File),
          ),
        quickActions = listOf("Search", "Files", "Think", "Voice"),
        isVoiceReady = true,
      ),
    workbenchShortcuts =
      listOf(
        WorkbenchShortcut(WorkbenchKind.Spec, "Spec", "Product plan"),
        WorkbenchShortcut(WorkbenchKind.CodeReview, "Code", "Diff tokens"),
        WorkbenchShortcut(WorkbenchKind.Handoff, "Handoff", "Desktop bridge"),
        WorkbenchShortcut(WorkbenchKind.Artifact, "Artifact", "Preview"),
        WorkbenchShortcut(WorkbenchKind.Writing, "Writing", "Draft polish"),
      ),
  )

private fun seedTopology() =
  FabricTopologyState(
    latestSequenceNumber = 42,
    hosts =
      listOf(
        HostTopology(
          hostId = "ryans-macbook-pro",
          workspaceId = "acme/mobile",
          agentRuns =
            listOf(
              AgentRunState(
                agentRunId = "composer-alpha",
                status = AgentStatus.ExecutingTool,
                activeTool = "gradle test",
                tokenPreview = "Building Cursor shell and verifying render models...",
                modifiedFiles = listOf("CursorNavDisplay.kt", "CodeDiffCard.kt"),
              )
            ),
        ),
        HostTopology(
          hostId = "cursor-cloud-worker",
          workspaceId = "shape-prototype",
          agentRuns =
            listOf(
              AgentRunState(
                agentRunId = "design-system",
                status = AgentStatus.Thinking,
                activeTool = null,
                tokenPreview = "Preparing reusable components for foldable scenes.",
                modifiedFiles = listOf("ComposerDock.kt", "WorkbenchCards.kt"),
              )
            ),
        ),
      ),
  )

private fun seedWorkbench(threadId: String, kind: WorkbenchKind): WorkbenchState =
  when (kind) {
    WorkbenchKind.Spec -> specWorkbench(threadId)
    WorkbenchKind.CodeReview -> codeReviewWorkbench(threadId)
    WorkbenchKind.Handoff -> handoffWorkbench(threadId)
    WorkbenchKind.Artifact -> artifactWorkbench(threadId)
    WorkbenchKind.Writing -> writingWorkbench(threadId)
  }

private fun specWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Spec,
    status = "Planning",
    title = "Mobile agent controller spec",
    summary = "Define the core loop before connecting production streams.",
    spec =
      SpecWorkbench(
        sections =
          listOf(
            SpecSection("Conversation", "One chat timeline owns user intent and agent responses."),
            SpecSection("Workbench", "Typed panes project specs, diffs, handoffs, artifacts, and writing."),
            SpecSection("Fabric", "Repository state mirrors host topology, agent runs, and sequence catchup."),
          ),
        nextSteps =
          listOf(
            ChecklistItem("Keep phone and foldable on one back stack", true),
            ChecklistItem("Render workbench inline on narrow screens", false),
            ChecklistItem("Swap fake repository for Room and gRPC later", false),
          ),
      ),
    codeReview = null,
    handoff = null,
    artifact = null,
    draft = null,
  )

private fun codeReviewWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.CodeReview,
    status = "Reviewing",
    title = "Diff render pipeline",
    summary = "Incoming unified diff chunks are transformed into immutable visual rows.",
    spec = null,
    codeReview =
      CodeReviewWorkbench(
        plan =
          listOf(
            ChecklistItem("Parse diff deltas off the UI path", true),
            ChecklistItem("Render additions and removals as stable rows", true),
            ChecklistItem("Persist packets by sequence number later", false),
          ),
        diff =
          CodeDiffRenderParser.fromDeltas(
            listOf(
              DiffDelta(
                filePath = "app/src/main/java/com/example/cursor/model/CodeDiffRenderModel.kt",
                lineStart = 18,
                hunks =
                  listOf(
                    "@@ render tokens @@",
                    "- val rawDiff: String",
                    "+ val lines: List<DiffLineRenderModel>",
                    "+ val kind: DiffLineKind",
                    "  val filePath: String",
                  ),
              )
            )
          ),
        files = listOf("CodeDiffRenderModel.kt", "CodeDiffCard.kt", "FakeFabricRepository.kt"),
      ),
    handoff = null,
    artifact = null,
    draft = null,
  )

private fun handoffWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Handoff,
    status = "Ready",
    title = "Desktop handoff",
    summary = "Send the active thread and workbench context to Cursor Desktop.",
    spec = null,
    codeReview = null,
    handoff =
      HandoffWorkbench(
        targetDevice = "Ryan's MacBook Pro",
        items =
          listOf(
            ChecklistItem("Thread context serialized", true),
            ChecklistItem("Workspace tunnel available", true),
            ChecklistItem("Awaiting user confirmation", false),
          ),
      ),
    artifact = null,
    draft = null,
  )

private fun artifactWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Artifact,
    status = "Preview",
    title = "Flashcards artifact",
    summary = "A compact artifact preview lives in the workbench without replacing chat.",
    spec = null,
    codeReview = null,
    handoff = null,
    artifact =
      ArtifactWorkbench(
        name = "NeuroCards",
        subtitle = "Study smarter",
        progress = 0.45f,
        previewLines =
          listOf(
            "Question 3 of 12",
            "What is the function of the myelin sheath?",
            "B. Increase impulse conduction speed",
          ),
      ),
    draft = null,
  )

private fun writingWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Writing,
    status = "Drafting",
    title = "Interview follow-up",
    summary = "Tone variants share the same workbench contract as specs and diffs.",
    spec = null,
    codeReview = null,
    handoff = null,
    artifact = null,
    draft =
      DraftWorkbench(
        selectedTone = "Warm and gracious",
        tones = listOf("Warm", "Confident", "PM concise"),
        subject = "Thank you - Product Manager interview",
        body =
          "Hi Jordan,\n\nThank you again for taking the time to speak with me yesterday. I enjoyed learning more about the team roadmap and customer impact.\n\nWarmly,\nAlex",
        rationale = "Warm, appreciative, and concise enough for a mobile review pass.",
      ),
  )
