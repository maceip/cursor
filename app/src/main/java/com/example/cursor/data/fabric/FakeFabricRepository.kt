package com.example.cursor.data.fabric

import com.example.cursor.data.fabric.FabricPayload.DiffDelta
import com.example.cursor.data.fabric.FabricPayload.Status
import com.example.cursor.data.fabric.FabricPayload.TokenChunk
import com.example.cursor.model.ArtifactPreviewState
import com.example.cursor.model.AttachmentChipModel
import com.example.cursor.model.AutocompleteSuggestion
import com.example.cursor.model.ChatMessage
import com.example.cursor.model.CodeDiffCardState
import com.example.cursor.model.ComposerState
import com.example.cursor.model.CursorThreadState
import com.example.cursor.model.CursorWorkspaceState
import com.example.cursor.model.DraftCardState
import com.example.cursor.model.FileReference
import com.example.cursor.model.HandoffCardState
import com.example.cursor.model.MessageAuthor
import com.example.cursor.model.PromptToken
import com.example.cursor.model.PromptTokenKind
import com.example.cursor.model.QuizOption
import com.example.cursor.model.SpecBullet
import com.example.cursor.model.SpecCardState
import com.example.cursor.model.TaskItem
import com.example.cursor.model.WorkbenchState
import com.example.cursor.nav.WorkbenchKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFabricRepository : FabricRepository {
  private val packets = demoPackets()
  private val _topology = MutableStateFlow<Map<String, HostTopology>>(emptyMap())
  override val topology: StateFlow<Map<String, HostTopology>> = _topology.asStateFlow()

  private val _workspaceState = MutableStateFlow(demoWorkspaceState())
  override val workspaceState: StateFlow<CursorWorkspaceState> = _workspaceState.asStateFlow()

  override fun connect(lastKnownSequence: Long) {
    _topology.value =
      packets
        .filter { packet -> packet.sequenceNumber > lastKnownSequence }
        .fold(_topology.value, FabricTopologyReducer::reduce)
  }

  override fun disconnect() = Unit
}

object FakeFabricFixtures {
  fun workspaceState(): CursorWorkspaceState = demoWorkspaceState()
}

private const val DemoThreadId = "thread-flashcards"

private fun demoWorkspaceState(): CursorWorkspaceState {
  val thread =
    CursorThreadState(
      threadId = DemoThreadId,
      workspaceName = "Acme Workspace",
      modelName = "Cursor Pro - GPT-4.1",
      messages =
        listOf(
          ChatMessage(
            id = "m1",
            author = MessageAuthor.User,
            text = "Help me plan, build, review, hand off, and polish a mobile flashcards app.",
            timestampLabel = "10:12 AM",
          ),
          ChatMessage(
            id = "m2",
            author = MessageAuthor.Cursor,
            text = "I have a Cursor-ready skeleton: spec, diff review, desktop handoff, artifact preview, and draft refinement all share one thread.",
            timestampLabel = "10:13 AM",
          ),
        ),
      composer =
        ComposerState(
          placeholder = "Ask Cursor, attach context, or type @ to mention",
          attachments =
            listOf(
              AttachmentChipModel("product-brief.md", "Workspace file"),
              AttachmentChipModel("Figma frame", "Reference"),
            ),
          promptTokens =
            listOf(
              PromptToken("@workspace", PromptTokenKind.Mention),
              PromptToken("/handoff", PromptTokenKind.SlashCommand),
            ),
          autocompleteSuggestions =
            listOf(
              AutocompleteSuggestion("@agent-run", "Attach active cloud agent"),
              AutocompleteSuggestion("@diff", "Reference latest code changes"),
            ),
        ),
    )

  return CursorWorkspaceState(
    thread = thread,
    workbenches =
      mapOf(
        WorkbenchKind.Spec to specWorkbench(thread.threadId),
        WorkbenchKind.CodeReview to codeReviewWorkbench(thread.threadId),
        WorkbenchKind.Handoff to handoffWorkbench(thread.threadId),
        WorkbenchKind.Artifact to artifactWorkbench(thread.threadId),
        WorkbenchKind.Writing to writingWorkbench(thread.threadId),
      ),
  )
}

private fun specWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Spec,
    title = "Spec planning",
    summary = "Define the MVP and study loop before implementation.",
    statusLabel = "Ready for review",
    spec =
      SpecCardState(
        title = "Flashcards product spec",
        bullets =
          listOf(
            SpecBullet("Problem", "Students need fast mobile recall practice with offline support."),
            SpecBullet("Core loop", "Create decks, review cards, grade recall, and resurface weak topics."),
            SpecBullet("MVP", "Decks, spaced repetition, progress, sync-ready storage, and accessibility."),
          ),
        nextSteps =
          listOf(
            TaskItem("Confirm target users"),
            TaskItem("Lock MVP scope"),
            TaskItem("Review technical constraints", complete = true),
          ),
      ),
  )

private fun codeReviewWorkbench(threadId: String): WorkbenchState {
  val diff =
    FabricTopologyReducer.reduce(emptyMap(), demoPackets().last()).values.first().activeAgents.values.first().modifiedFiles.values.toList()
  return WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.CodeReview,
    title = "Code review",
    summary = "Timer restart clears its previous interval before creating a new one.",
    statusLabel = "Patch staged",
    diff =
      CodeDiffCardState(
        title = "src/hooks/useTimer.ts",
        files = diff,
        touchedFiles =
          listOf(
            FileReference("useTimer.ts", "src/hooks"),
            FileReference("TimerView.tsx", "src/components"),
            FileReference("useTimer.test.ts", "src/hooks"),
          ),
      ),
  )
}

private fun handoffWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Handoff,
    title = "Desktop handoff",
    summary = "Continue on Cursor Desktop with the recap deck already attached.",
    statusLabel = "Waiting on desktop",
    handoff =
      HandoffCardState(
        targetDevice = "Ryan's MacBook Pro",
        message = "The PDF is ready on desktop for final review before sending.",
        tasks =
          listOf(
            TaskItem("Found recap-one-pager.pdf", "412 KB", complete = true),
            TaskItem("Attached to Product Strategy Sync", "Today, 2:00 PM", complete = true),
            TaskItem("Open draft on Cursor Desktop"),
          ),
      ),
  )

private fun artifactWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Artifact,
    title = "Artifact preview",
    summary = "A native-feeling study card prototype is ready to run.",
    statusLabel = "Runnable",
    artifact =
      ArtifactPreviewState(
        appName = "NeuroCards",
        subtitle = "Study smarter",
        progressLabel = "Question 3 of 12",
        question = "What is the function of the myelin sheath?",
        options =
          listOf(
            QuizOption("A", "Generate action potentials"),
            QuizOption("B", "Increase the speed of impulse conduction", selected = true),
            QuizOption("C", "Produce neurotransmitters"),
          ),
      ),
  )

private fun writingWorkbench(threadId: String) =
  WorkbenchState(
    threadId = threadId,
    kind = WorkbenchKind.Writing,
    title = "Writing refinement",
    summary = "Choose a tone and keep the follow-up concise.",
    statusLabel = "Draft ready",
    draft =
      DraftCardState(
        selectedTone = "Warm and gracious",
        tones = listOf("Warm and gracious", "Confident", "PM concise"),
        subject = "Thank you - Product Manager interview",
        body =
          "Hi Jordan,\n\nThank you again for taking the time to speak with me yesterday. I enjoyed learning more about the team's roadmap and how you are approaching customer impact at scale.\n\nWarmly,\nAlex",
        rationale = "Warm, appreciative language builds rapport while keeping the note concise.",
      ),
  )

private fun demoPackets() =
  listOf(
    FabricPacket(
      packetId = "packet-1",
      sequenceNumber = 1,
      timestampMs = 1_719_150_000,
      hostId = "ryans-macbook-pro",
      workspaceId = "neurocards",
      agentRunId = "composer-worker-alpha",
      payload = Status(AgentRunStatus.ExecutingTool, "edit"),
    ),
    FabricPacket(
      packetId = "packet-2",
      sequenceNumber = 2,
      timestampMs = 1_719_150_100,
      hostId = "ryans-macbook-pro",
      workspaceId = "neurocards",
      agentRunId = "composer-worker-alpha",
      payload = TokenChunk("Clearing the previous interval before restart. "),
    ),
    FabricPacket(
      packetId = "packet-3",
      sequenceNumber = 3,
      timestampMs = 1_719_150_200,
      hostId = "ryans-macbook-pro",
      workspaceId = "neurocards",
      agentRunId = "composer-worker-alpha",
      payload =
        DiffDelta(
          filePath = "src/hooks/useTimer.ts",
          lineStart = 42,
          hunks =
            listOf(
              "@@ -42,4 +42,7 @@",
              " const start = () => {",
              "+  if (intervalRef.current) {",
              "+    clearInterval(intervalRef.current)",
              "+  }",
              "   setIsRunning(true)",
              "   intervalRef.current = setInterval(() => {",
            ),
        ),
    ),
  )
