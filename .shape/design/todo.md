Nav3 Scene concepts make this much cleaner.

The refined architecture becomes:

Design tokens
→ reusable UI components
→ typed NavKeys
→ NavEntries with metadata
→ SceneStrategies that choose phone vs foldable layout

The important shift: phone and foldable are not two separate implementations. They are different scenes over the same back stack.

Android’s Navigation 3 is built around a developer-owned back stack, NavDisplay, typed keys, and scene strategies that can read more than one back-stack entry at once for adaptive layouts. That is exactly our case: phone shows one active entry, foldable can show chat context plus active workbench content together.

The better mental model

Instead of:

PhonePlanningScreen()
FoldPlanningScreen()
PhoneCodingScreen()
FoldCodingScreen()

Do this:

NavKey: ConversationKey
NavKey: WorkbenchKey

Phone scene:
  render latest key as one-pane chat stream

Foldable scene:
  render ConversationKey + WorkbenchKey together

So the back stack might look like:

[ConversationKey(threadId), WorkbenchKey(threadId, WorkbenchKind.CodeReview)]

On a narrow phone, NavDisplay renders the latest key as a single screen.

On an unfolded Pixel Fold, a custom SceneStrategy can consume the last two compatible entries and render them as one two-pane scene.

Google’s two-pane Nav3 recipe uses this exact idea: a two-pane scene is returned when the window is wide enough and the relevant back-stack entries declare through metadata that they support being displayed together.

Updated file structure
ui/
  theme/
    CursorClaudeTheme.kt
    CursorClaudeTokens.kt

  components/
    CursorHeader.kt
    ComposerDock.kt
    ChatPane.kt
    WorkbenchCards.kt
    SpecCards.kt
    CodeDiffCard.kt
    HandoffCard.kt
    ArtifactPreview.kt
    DraftCard.kt

nav/
  CursorNavKeys.kt
  CursorNavEntries.kt
  CursorScenes.kt
  CursorSceneMetadata.kt
  CursorNavDisplay.kt

model/
  ConversationState.kt
  WorkbenchState.kt

The big new thing is nav/CursorScenes.kt.

Typed NavKeys

Something like:

@Serializable
data class ConversationKey(
    val threadId: String
) : NavKey

@Serializable
data class WorkbenchKey(
    val threadId: String,
    val kind: WorkbenchKind
) : NavKey

@Serializable
enum class WorkbenchKind {
    Spec,
    CodeReview,
    Handoff,
    Artifact,
    Writing
}

This is better than encoding “screen name” because the foldable layout needs to know what kind of right-pane workbench to render.

Metadata tells Nav3 which entries can pair

Nav3 metadata is the bridge between entries, scenes, and NavDisplay; Android’s docs describe metadata as arbitrary information shared between NavEntry, Scene, and NavDisplay.

Conceptually:

object CursorSceneMetadata {
    const val PaneRole = "paneRole"
    const val ThreadId = "threadId"
}

enum class PaneRole {
    Conversation,
    Workbench
}

Then your entries say:

NavEntry(
    key = ConversationKey(threadId),
    metadata = mapOf(
        CursorSceneMetadata.PaneRole to PaneRole.Conversation,
        CursorSceneMetadata.ThreadId to threadId
    )
) {
    ConversationPane(threadId)
}

And:

NavEntry(
    key = WorkbenchKey(threadId, WorkbenchKind.CodeReview),
    metadata = mapOf(
        CursorSceneMetadata.PaneRole to PaneRole.Workbench,
        CursorSceneMetadata.ThreadId to threadId
    )
) {
    CodeReviewWorkbench(threadId)
}

Now the scene strategy can ask:

Are the last two entries:
- same thread?
- one Conversation?
- one Workbench?
- window width > fold threshold?

If yes: render a custom two-pane scene.

If no: fall back to single-pane.

The SceneStrategy becomes the foldable brain

Pseudo-code:

class CursorFoldableSceneStrategy(
    private val windowWidthDp: Dp
) : SceneStrategy<NavKey> {

    override fun calculateScene(
        entries: List<NavEntry<NavKey>>,
        onBack: () -> Unit
    ): Scene<NavKey>? {
        if (windowWidthDp < 600.dp) return null

        val last = entries.lastOrNull() ?: return null
        val previous = entries.dropLast(1).lastOrNull() ?: return null

        val pair = CursorPanePair.from(previous, last) ?: return null

        return CursorTwoPaneScene(
            key = "cursor-two-pane-${pair.threadId}-${pair.workbenchKind}",
            entries = listOf(pair.conversationEntry, pair.workbenchEntry),
            conversation = {
                pair.conversationEntry.Content()
            },
            workbench = {
                pair.workbenchEntry.Content()
            }
        )
    }
}

The exact Nav3 APIs may need adjusting to match the version you install, but architecturally this is the right shape.

Why this fits our mockups perfectly

The five fold renders are not simply wide versions of the phone screens. They are two-pane scenes:

Left NavEntry:
  ConversationKey(threadId)

Right NavEntry:
  WorkbenchKey(threadId, kind)

For each image:

Mockup	Left entry	Right entry
Planning	ConversationKey	WorkbenchKey(kind = Spec)
Coding	ConversationKey	WorkbenchKey(kind = CodeReview)
Handoff	ConversationKey	WorkbenchKey(kind = Handoff)
Artifact	ConversationKey	WorkbenchKey(kind = Artifact)
Writing	ConversationKey	WorkbenchKey(kind = Writing)

That gives us one navigation model that adapts naturally.

Phone behavior

On phone, the same back stack can show the latest destination:

[ConversationKey, WorkbenchKey(Spec)]

Phone scene renders:

WorkbenchKey(Spec) as a single-column chat stream:
  header
  conversation context
  spec card inline
  composer

Or, if you want the Claude feel to stay more chat-native, the phone entry can render ConversationWithInlineWorkbench.

That is a design decision.

I would do this:

@Composable
fun WorkbenchEntryContent(
    key: WorkbenchKey,
    layoutMode: LayoutMode
) {
    when (layoutMode) {
        LayoutMode.Phone -> PhoneConversationWithInlineWorkbench(key)
        LayoutMode.Foldable -> WorkbenchPane(key)
    }
}

The content is the same. The placement changes.

Foldable behavior

On unfolded Pixel Fold, the custom scene renders both entries:

@Composable
fun CursorTwoPaneScene(
    conversation: @Composable () -> Unit,
    workbench: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
            ) {
                conversation()
            }

            FoldGutter()

            Box(
                Modifier
                    .weight(0.62f)
                    .fillMaxHeight()
            ) {
                workbench()
            }
        }

        ComposerDock(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

The center crease becomes the scene boundary.

Use built-in scene strategies where possible

There are two options:

Option A: Use Material 3 adaptive scene strategies

The Compose Material 3 adaptive release notes mention ListDetailSceneStrategy, rememberListDetailSceneStrategy, SupportingPaneSceneStrategy, and rememberSupportingPaneSceneStrategy for canonical pane scaffolds.

This is good if our UI maps cleanly to:

list/detail
supporting pane

But our UI is more specific:

conversation pane + workbench pane + persistent composer dock
Option B: Custom Cursor SceneStrategy

This is what I’d use.

Reason: we need a custom bottom composer dock, custom left/right proportions, hinge-safe behavior, and specific pairing rules.

Refined Cursor prompt

Use this now:

Implement the mobile and unfolded Pixel Fold mockups using Jetpack Compose and Navigation 3 scene concepts.

Do not create 10 separate screens.

Create a Nav3-based architecture:

1. Typed NavKeys:
   - ConversationKey(threadId)
   - WorkbenchKey(threadId, kind)
   - optional SettingsKey / AttachmentPickerKey later

2. WorkbenchKind enum:
   - Spec
   - CodeReview
   - Handoff
   - Artifact
   - Writing

3. NavEntries:
   - ConversationKey renders the chat context pane.
   - WorkbenchKey renders the active output/workbench content.
   - Entries include metadata describing pane role and thread id.

4. Custom SceneStrategy:
   - On narrow width, fall back to single-pane rendering.
   - On unfolded/foldable width, if the last two entries are a ConversationKey and WorkbenchKey for the same thread, render them together as CursorTwoPaneScene.
   - Left pane is conversation context, about 38%.
   - Right pane is active workbench, about 62%.
   - Keep composer dock persistent at bottom.
   - Treat the fold crease as the gutter between panes.

5. Content model:
   - Five conversation/workbench states:
     Spec, CodeReview, Handoff, Artifact, Writing.
   - Do not duplicate data for phone and foldable.
   - Phone renders workbench content inline inside the chat stream.
   - Foldable renders workbench content in the right pane.

6. Visual design:
   - Cursor-branded Claude-like interface.
   - Cream background.
   - Charcoal text.
   - Rounded cards.
   - Thin borders.
   - Compact chips.
   - Mobile-native touch targets.
   - Not desktop.

Deliver:
- CursorClaudeTheme.kt
- CursorNavKeys.kt
- CursorNavEntries.kt
- CursorSceneMetadata.kt
- CursorScenes.kt
- CursorNavDisplay.kt
- PhoneConversationLayout.kt
- FoldableWorkbenchLayout.kt
- Preview composables for all five states in both phone and unfolded modes.
The cleanest final architecture
Back stack:
  ConversationKey(thread-123)
  WorkbenchKey(thread-123, Artifact)

Phone Scene:
  SinglePaneScene
  → shows Artifact as inline chat response

Foldable Scene:
  CursorTwoPaneScene
  → left: ConversationKey
  → right: WorkbenchKey(Artifact)
  → bottom: ComposerDock

This is much better than a hand-rolled if (width > 600) inside every screen.

Nav3 Scenes let the navigation layer decide how many entries become visible, while your components stay dumb and reusable. That is exactly what we want for “mobile and foldable from the same product system.”


1. Make a tiny Cursor/Claude Compose design system

Create these files:

app/src/main/java/.../ui/
  CursorClaudeTheme.kt
  CursorClaudeComponents.kt
  CursorClaudeScreens.kt
2. Use the images as a visual spec, not source code

Tell Cursor:

Using the attached five mockups as the visual target, implement them in Kotlin Jetpack Compose.

Do not try to exactly OCR every pixel. Instead create a reusable design system:
- cream/off-white background
- charcoal text
- rounded cards
- thin borders
- soft gray chips
- bottom composer
- Android-native status/header spacing
- Cursor-like logo/header
- Claude-like chat mechanics

Build five screen states from reusable composables:
1. planning/spec response
2. coding/debug response
3. desktop handoff
4. artifact preview
5. writing refinement

Use Material 3 Compose components where useful, but override colors/shapes to match the mockups.
3. Starter Compose code

This gets you 70% of the way immediately.

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cursorclaude

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val Cream = Color(0xFFFAF8F3)
private val Card = Color(0xFFFEFCF8)
private val Soft = Color(0xFFF2F0EA)
private val Stroke = Color(0xFFE1DED6)
private val Ink = Color(0xFF171717)
private val Muted = Color(0xFF77746D)
private val CursorBlue = Color(0xFF2F7DF6)
private val CodeGreen = Color(0xFFEAF5E8)
private val AccentRust = Color(0xFFC85F36)

@Composable
fun CursorClaudeTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        background = Cream,
        surface = Card,
        surfaceVariant = Soft,
        primary = Ink,
        onPrimary = Color.White,
        onBackground = Ink,
        onSurface = Ink,
        outline = Stroke
    )

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 25.sp
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            titleMedium = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        ),
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}

@Composable
fun CursorClaudeApp(screen: DemoScreen = DemoScreen.Planning) {
    CursorClaudeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Cream)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                Header()
                Spacer(Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    item {
                        UserBubble(screen.userPrompt)
                    }

                    item {
                        AssistantBlock {
                            when (screen) {
                                DemoScreen.Planning -> PlanningResponse()
                                DemoScreen.Coding -> CodingResponse()
                                DemoScreen.Handoff -> HandoffResponse()
                                DemoScreen.Artifact -> ArtifactResponse()
                                DemoScreen.Writing -> WritingResponse()
                            }
                        }
                    }
                }

                QuickActions()
                Spacer(Modifier.height(10.dp))
                Composer()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CursorCube()
            Spacer(Modifier.width(9.dp))
            Text(
                "CURSOR",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.History, null, tint = Ink)
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Outlined.MoreVert, null, tint = Ink)
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Pill("Acme Workspace", Icons.Outlined.KeyboardArrowDown)
            Pill("Cursor Pro · GPT-4.1", Icons.Outlined.KeyboardArrowDown)
        }
    }
}

@Composable
private fun CursorCube(size: Dp = 26.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.linearGradient(listOf(Color.Black, Color(0xFF7B7B7B))))
            .border(1.dp, Color.Black.copy(alpha = .15f), RoundedCornerShape(5.dp))
    )
}

@Composable
private fun Pill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Card,
        border = BorderStroke(1.dp, Stroke)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 14.sp)
            if (icon != null) {
                Spacer(Modifier.width(4.dp))
                Icon(icon, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Avatar("You")
        Spacer(Modifier.width(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Soft.copy(alpha = .75f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row {
                    Text("You", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Text("10:12 AM", color = Muted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AssistantBlock(content: @Composable ColumnScope.() -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Ink),
            contentAlignment = Alignment.Center
        ) {
            CursorCube(16.dp)
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cursor", fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(10.dp))
                Text("10:12 AM", color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ReviewCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.Article,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Card,
        border = BorderStroke(1.dp, Stroke)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(18.dp), tint = Ink)
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                trailing()
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PlanningResponse() {
    Text(
        "Sure—here’s a high-level plan for your mobile flashcards app. We’ll define the core experience, key features, and a phased roadmap.",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(14.dp))

    ReviewCard("Spec overview", Icons.Outlined.FormatListBulleted) {
        NumberedLine("1.", "Problem & goal", "Enable students to study anything, anywhere.")
        NumberedLine("2.", "Core experience", "Create → Study → Review → Master")
        NumberedLine("3.", "Key features", "Decks, flashcards, spaced repetition, progress, offline support.")
        NumberedLine("4.", "Roadmap", "MVP → Beta → 1.0 with AI study coach.")
    }

    Spacer(Modifier.height(10.dp))

    ReviewCard("Next steps", Icons.Outlined.CheckBox) {
        CheckLine("Confirm target users & success metrics", checked = false)
        CheckLine("Define MVP scope", checked = false)
        CheckLine("Draft information architecture", checked = false)
        CheckLine("Review technical constraints", checked = false)
    }

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SourceChip("Spaced repetition", "supermemo.com", Modifier.weight(1f))
        SourceChip("Mobile UX guide", "nngroup.com", Modifier.weight(1f))
    }
}

@Composable
private fun CodingResponse() {
    Text(
        "The issue is that you’re not clearing the interval when restarting. I’ll update the hook and clean up the previous interval.",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(14.dp))

    ReviewCard(
        title = "Plan",
        icon = Icons.Outlined.FormatListBulleted,
        trailing = { StatusChip("In progress") }
    ) {
        CheckLine("Review timer state logic in useTimer.ts", checked = true)
        CheckLine("Clear existing interval on restart")
        CheckLine("Reset elapsed time to 0")
        CheckLine("Add test to prevent regression")
    }

    Spacer(Modifier.height(10.dp))

    ReviewCard("src/hooks/useTimer.ts", Icons.Outlined.Code) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Soft.copy(alpha = .65f))
                .padding(12.dp)
        ) {
            Column {
                CodeLine("42  const start = () => {")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(CodeGreen, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Column {
                        CodeLine("+  if (intervalRef.current) {")
                        CodeLine("+    clearInterval(intervalRef.current)")
                        CodeLine("+  }")
                    }
                }
                CodeLine("46  setIsRunning(true)")
                CodeLine("47  intervalRef.current = setInterval(() => {")
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    ReviewCard("Relevant files", Icons.Outlined.Article) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FileChip("app.tsx", "src", Modifier.weight(1f))
            FileChip("useTimer.ts", "hooks", Modifier.weight(1f))
            FileChip("TimerView.tsx", "components", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HandoffResponse() {
    Text("Sure thing — pulling up the deck now.", style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(14.dp))

    ReviewCard("Sent to Cursor Desktop", Icons.Outlined.CheckCircle) {
        Text("Continue on your laptop to review and send.", color = Muted)
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.PhoneAndroid, null, Modifier.size(54.dp), tint = Muted)
            Text("⌁", fontSize = 44.sp, color = AccentRust)
            Icon(Icons.Outlined.LaptopMac, null, Modifier.size(68.dp), tint = Muted)
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue on desktop")
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ArrowOutward, null)
        }
    }

    Spacer(Modifier.height(10.dp))

    ReviewCard("Summary", Icons.Outlined.FormatListBulleted) {
        CheckLine("Found recap deck on your laptop", true, "recap-one-pager.pdf · 412 KB")
        CheckLine("Attached to 2 PM invite", true, "Product Strategy Sync · Today, 2:00 PM")
        CheckLine("Sent to Cursor Desktop", false, "Ready to review and send")
    }
}

@Composable
private fun ArtifactResponse() {
    Text(
        "Absolutely. I’ll build a simple flashcards app with spaced repetition, a clean review flow, and progress tracking.",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(14.dp))

    ReviewCard("Artifact preview", Icons.Outlined.ViewInAr) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF191919))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2F8F75))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("NeuroCards", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Study smarter", color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(50)) {
                        Text("Run", color = Color.White)
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("Question 3 of 12", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { .45f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = .15f)
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    "What is the function of the myelin sheath?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                DarkOption("A", "Generate action potentials")
                DarkOption("B", "Increase the speed of impulse conduction", selected = true)
                DarkOption("C", "Produce neurotransmitters")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("Open artifact", Icons.Outlined.ArrowOutward)
            Pill("Iterate", Icons.Outlined.Refresh)
        }
    }
}

@Composable
private fun WritingResponse() {
    Text(
        "I can refine your email with a few different tones. Choose the one that best fits your style and goal.",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToneChip("Warm and gracious", true, Modifier.weight(1f))
        ToneChip("Confident", false, Modifier.weight(1f))
        ToneChip("PM concise", false, Modifier.weight(1f))
    }

    Spacer(Modifier.height(12.dp))

    ReviewCard("Refined draft: Warm and gracious", Icons.Outlined.Mail) {
        Text("Subject", color = Muted, fontSize = 13.sp)
        Text("Thank you — Product Manager interview", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))
        Text("Body", color = Muted, fontSize = 13.sp)
        Text(
            "Hi Jordan,\n\nThank you again for taking the time to speak with me yesterday. I enjoyed learning more about the team’s roadmap and how you’re approaching customer impact at scale.\n\nWarmly,\nAlex",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(Modifier.height(10.dp))

    ReviewCard("Why this works", Icons.Outlined.AutoAwesome) {
        Text(
            "Warm, appreciative tone builds rapport while keeping the note concise and professional.",
            color = Ink
        )
    }
}

@Composable
private fun QuickActions() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill("Search", Icons.Outlined.Search)
        Pill("Files", Icons.Outlined.AttachFile)
        Pill("Think", Icons.Outlined.AutoAwesome)
        Pill("…")
    }
}

@Composable
private fun Composer() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Card,
        border = BorderStroke(1.dp, Stroke),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Ask anything or type @ to mention", color = Muted, fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.Outlined.Add, null) }
                IconButton(onClick = {}) { Icon(Icons.Outlined.AttachFile, null) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) { Icon(Icons.Outlined.Mic, null) }
                FloatingActionButton(
                    onClick = {},
                    containerColor = Ink,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.ArrowUpward, null)
                }
            }
        }
    }
}

@Composable
private fun Avatar(text: String) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Ink),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NumberedLine(n: String, title: String, body: String) {
    Row(Modifier.padding(vertical = 6.dp)) {
        Text(n, Modifier.width(34.dp), color = Ink)
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(body, color = Ink.copy(alpha = .85f))
        }
    }
}

@Composable
private fun CheckLine(text: String, checked: Boolean = false, subtext: String? = null) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Icon(
            if (checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            null,
            Modifier.size(18.dp),
            tint = if (checked) Ink else Muted
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(text)
            if (subtext != null) Text(subtext, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SourceChip(title: String, domain: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Card,
        border = BorderStroke(1.dp, Stroke)
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(Icons.Outlined.Article, null, Modifier.size(17.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(domain, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
private fun FileChip(name: String, path: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Soft.copy(alpha = .55f),
        border = BorderStroke(1.dp, Stroke)
    ) {
        Column(Modifier.padding(9.dp)) {
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(path, fontSize = 11.sp, color = Muted)
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF1EFE5),
        border = BorderStroke(1.dp, Stroke)
    ) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp)
    }
}

@Composable
private fun CodeLine(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
}

@Composable
private fun DarkOption(letter: String, text: String, selected: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = .08f),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFF3DBB9A) else Color.White.copy(alpha = .10f)
        )
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(letter, color = Color.White.copy(alpha = .8f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Text(text, color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ToneChip(text: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFFEAF3FF) else Card,
        border = BorderStroke(1.dp, if (selected) CursorBlue.copy(alpha = .35f) else Stroke)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            fontSize = 12.sp,
            color = if (selected) CursorBlue else Ink,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

enum class DemoScreen(val userPrompt: String) {
    Planning("Help me plan a product spec for a mobile flashcards app for students."),
    Coding("I’m seeing a bug where the timer won’t reset when I start it again. Can you help me fix this?"),
    Handoff("I’m running late to a meeting. Can you attach my recap deck one-pager from my laptop as a PDF and attach it to my 2 PM invite?"),
    Artifact("Can you build a small flashcards or quiz app that helps me study neuroscience?"),
    Writing("How can I improve this interview follow-up email to be more professional?")
}
4. Then wire the five screens

In MainActivity.kt:

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf(DemoScreen.Planning) }

            CursorClaudeTheme {
                Column {
                    CursorClaudeApp(screen)
                }
            }
        }
    }
}

For a real prototype, add a hidden debug switcher:

val screens = DemoScreen.entries

Row {
    screens.forEach { s ->
        TextButton(onClick = { screen = s }) {
            Text(s.name)
        }
    }
}
5. The real “snap into reality” prompt for Cursor

Paste this into Cursor with the five images attached:

Take these five Android mockups and convert them into a Jetpack Compose prototype.

Important:
- Do not create five unrelated screens.
- Build a reusable component system.
- Match the design language: cream background, charcoal text, thin borders, rounded cards, compact chips, bottom composer, Cursor-like header.
- Use Material 3 Compose as the base.
- Build static screen states first; no backend.
- Use Preview composables for each screen.
- Keep all copy and cards editable through data models.
- Make it responsive for Pixel phones and foldables.
- Extract design tokens into CursorClaudeTheme.kt.
- Extract cards/chips/composer/header into CursorClaudeComponents.kt.
- Put screen states into CursorClaudeScreens.kt.

Deliver:
1. Compilable Kotlin Compose code.
2. Five @Preview functions.
3. One MainActivity that can switch between the five screens.

The critical trick: don’t ask it to “generate UI from image.” Ask it to extract the system. The images are excellent because they define a coherent system: header, chat rhythm, response cards, pills, source chips, code blocks, handoff cards, artifact preview, writing variants, and composer. That maps beautifully to Compose.
