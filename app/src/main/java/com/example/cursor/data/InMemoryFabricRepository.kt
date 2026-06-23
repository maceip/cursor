package com.example.cursor.data

import com.example.cursor.model.ArtifactChoice
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
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricTopologyProjector
import com.example.cursor.model.FabricTopologyState
import com.example.cursor.model.HandoffWorkbench
import com.example.cursor.model.MessageAuthor
import com.example.cursor.model.PromptToken
import com.example.cursor.model.PromptTokenKind
import com.example.cursor.model.SourceCard
import com.example.cursor.model.SpecSection
import com.example.cursor.model.SpecWorkbench
import com.example.cursor.model.WorkbenchKind
import com.example.cursor.model.WorkbenchShortcut
import com.example.cursor.model.WorkbenchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryFabricRepository(
  private val threadId: String = FabricDefaults.DefaultThreadId,
  initialKind: WorkbenchKind = WorkbenchKind.Spec,
) : FabricRepository {
  private val _conversation = MutableStateFlow(seedConversation(threadId, initialKind))
  override val conversation: StateFlow<ConversationState> = _conversation.asStateFlow()

  private val _activeWorkbench = MutableStateFlow(seedWorkbench(threadId, initialKind))
  override val activeWorkbench: StateFlow<WorkbenchState> = _activeWorkbench.asStateFlow()

  private val _packets = MutableStateFlow(seedFabricPackets())
  override val packets: StateFlow<List<FabricPacket>> = _packets.asStateFlow()

  private val _topology = MutableStateFlow(FabricTopologyProjector.fromPackets(_packets.value))
  override val topology: StateFlow<FabricTopologyState> = _topology.asStateFlow()

  override fun connect() = Unit

  override fun disconnect() = Unit

  override fun openWorkbench(kind: WorkbenchKind) {
    _conversation.value = seedConversation(threadId, kind)
    _activeWorkbench.value = seedWorkbench(threadId, kind)
  }

  override fun submitUserMessage(text: String) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return

    val timestampMs = System.currentTimeMillis()
    _conversation.value = _conversation.value.withUserMessage(trimmed, timestampMs)
  }

  override fun approveInteraction(interactionId: String, approved: Boolean, messageOverride: String?) = Unit

  override fun cancelAgentRun(agentRunId: String) = Unit

  override fun packetsAfter(sequenceNumber: Long): Flow<List<FabricPacket>> =
    packets.map { packets -> packets.filter { it.sequenceNumber > sequenceNumber } }

  override suspend fun appendPacket(packet: FabricPacket) {
    _packets.update { packets ->
      (packets.filterNot { it.packetId == packet.packetId || it.sequenceNumber == packet.sequenceNumber } + packet)
        .sortedBy { it.sequenceNumber }
    }
    _topology.value = FabricTopologyProjector.fromPackets(_packets.value)
  }
}

internal fun seedConversation(threadId: String, kind: WorkbenchKind = WorkbenchKind.Spec): ConversationState =
  ConversationState(
    threadId = threadId,
    title = kind.label,
    workspaceName = "Acme Workspace",
    modelName = if (kind == WorkbenchKind.CodeReview) "Claude 3.5 Sonnet" else "Cursor Pro - GPT-4.1",
    messages = messagesFor(kind),
    composer =
      ComposerState(
        promptHint = "Ask anything or type @ to mention",
        attachments =
          if (kind == WorkbenchKind.Handoff) listOf(AttachmentChip("recap-one-pager.pdf", "412 KB")) else emptyList(),
        tokens = emptyList(),
        quickActions = listOf("Search", "Files", "Think", "More"),
        isVoiceReady = true,
      ),
    workbenchShortcuts = WorkbenchKind.entries.map { WorkbenchShortcut(it, it.label, it.shortLabel) },
  )

private fun messagesFor(kind: WorkbenchKind): List<ConversationMessage> =
  when (kind) {
    WorkbenchKind.Spec ->
      listOf(
        ConversationMessage(
          id = "spec-user",
          author = MessageAuthor.User,
          body = "Help me plan a product spec for a mobile flashcards app for students.",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "spec-cursor",
          author = MessageAuthor.Cursor,
          body = "Sure - here's a high-level plan for your mobile flashcards app. We'll define the core experience, key features, and a phased roadmap.",
          timestamp = "10:12 AM",
        ),
      )
    WorkbenchKind.CodeReview ->
      listOf(
        ConversationMessage(
          id = "code-user",
          author = MessageAuthor.User,
          body = "I'm seeing a bug where the timer won't reset when I start it again. Can you help me fix this?",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "code-cursor",
          author = MessageAuthor.Cursor,
          body = "The issue is that you're not clearing the interval when restarting. I'll update the hook to properly reset state and clean up the previous interval.",
          timestamp = "10:13 AM",
        ),
      )
    WorkbenchKind.Handoff ->
      listOf(
        ConversationMessage(
          id = "handoff-user",
          author = MessageAuthor.User,
          body = "I'm running late to a meeting. Can you attach my recap deck one-pager from my laptop as a PDF and attach it to my 2 PM invite?",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "handoff-cursor",
          author = MessageAuthor.Cursor,
          body = "Sure thing - pulling up the deck now.",
          timestamp = "10:12 AM",
        ),
      )
    WorkbenchKind.Artifact ->
      listOf(
        ConversationMessage(
          id = "artifact-user",
          author = MessageAuthor.User,
          body = "Can you build a small flashcards or quiz app that helps me study neuroscience?",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "artifact-cursor",
          author = MessageAuthor.Cursor,
          body = "Absolutely! I'll build a simple flashcards app with spaced repetition, a clean review flow, and progress tracking.\n\nHere's a preview of the interactive app.",
          timestamp = "10:12 AM",
        ),
      )
    WorkbenchKind.Writing ->
      listOf(
        ConversationMessage(
          id = "writing-user",
          author = MessageAuthor.User,
          body = "How can I improve this interview follow-up email to be more professional?\n\nI drafted two versions - a warm, gracious note and a more confident, concise one. Pick whichever fits.",
          timestamp = "10:12 AM",
        ),
        ConversationMessage(
          id = "writing-cursor",
          author = MessageAuthor.Cursor,
          body = "I can refine your email with a few different tones. Choose the one that best fits your style and goal.",
          timestamp = "10:12 AM",
        ),
      )
  }

internal fun seedWorkbench(threadId: String, kind: WorkbenchKind): WorkbenchState =
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
            ChecklistItem("Confirm target users and success metrics", false),
            ChecklistItem("Define MVP scope", false),
            ChecklistItem("Draft information architecture", false),
            ChecklistItem("Review technical constraints", false),
          ),
        sources =
          listOf(
            SourceCard("Spaced repetition best practices", "supermemo.com"),
            SourceCard("Mobile learning UX guide", "nngroup.com"),
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
            ChecklistItem("Review timer state logic in useTimer.ts", true),
            ChecklistItem("Clear existing interval on restart", false),
            ChecklistItem("Reset elapsed time to 0", false),
            ChecklistItem("Add test to prevent regression", false),
          ),
        diff =
          CodeDiffRenderParser.fromDeltas(
            listOf(
              DiffDelta(
                filePath = "src/hooks/useTimer.ts",
                lineStart = 42,
                hunks =
                  listOf(
                    "@@ start timer @@",
                    "  const start = () => {",
                    "+   if (intervalRef.current) {",
                    "+     clearInterval(intervalRef.current)",
                    "+   }",
                    "    setIsRunning(true)",
                    "    intervalRef.current = setInterval(() => {",
                    "      setElapsed((t) => t + 1000)",
                    "    }, 1000)",
                  ),
              )
            )
          ),
        files = listOf("app.tsx", "useTimer.ts", "TimerView.tsx"),
        readyTitle = "Ready for review",
        readyBody = "I can run tests or open a PR when you're ready.",
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
            ChecklistItem("Found recap deck on your laptop\nrecap-one-pager.pdf - 412 KB", true),
            ChecklistItem("Attached to 2 PM invite\nProduct Strategy Sync - Today, 2:00 PM", true),
            ChecklistItem("Sent to Cursor Desktop\nReady to review and send", false),
          ),
        primaryAction = "Continue on desktop",
        sentAt = "Handoff sent at 10:12 AM",
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
        questionLabel = "Question 3 of 12",
        category = "Neuroanatomy",
        question = "What is the function of the myelin sheath?",
        choices =
          listOf(
            ArtifactChoice("A", "Generate action potentials", false),
            ArtifactChoice("B", "Increase the speed of impulse conduction", true),
            ArtifactChoice("C", "Produce neurotransmitters", false),
            ArtifactChoice("D", "Store calcium ions", false),
          ),
        footerStart = "Spaced repetition on",
        footerEnd = "Streak: 4",
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
        tones = listOf("Warm and gracious", "Confident and concise", "Product manager concise"),
        subject = "Thank you - Product Manager interview",
        body =
          "Hi Jordan,\n\nThank you again for taking the time to speak with me yesterday. I enjoyed learning more about the team's roadmap and how you're approaching customer impact at scale.\n\nI'm even more excited about the opportunity to contribute and would love to stay in touch as the process moves forward.\n\nWarmly,\nAlex",
        rationale = "Warm, appreciative tone builds rapport and reinforces enthusiasm while keeping the note concise and professional.",
      ),
  )

private val WorkbenchKind.shortLabel: String
  get() =
    when (this) {
      WorkbenchKind.Spec -> "Product plan"
      WorkbenchKind.CodeReview -> "Diff"
      WorkbenchKind.Handoff -> "Desktop"
      WorkbenchKind.Artifact -> "Preview"
      WorkbenchKind.Writing -> "Draft"
    }

internal val WorkbenchKind.label: String
  get() =
    when (this) {
      WorkbenchKind.Spec -> "Spec"
      WorkbenchKind.CodeReview -> "Code"
      WorkbenchKind.Handoff -> "Handoff"
      WorkbenchKind.Artifact -> "Artifact"
      WorkbenchKind.Writing -> "Writing"
    }
