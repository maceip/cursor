# Compose Preview Design Review

Review source: `.shape/design/*.png`
Export source: `app/src/screenshotTestDebug/reference/com/example/cursor/ui/shell/CursorShellScreenshotPreviewsKt/*.png`

## Scene Checks

- `ScreenshotPhoneSpec`: aligns with the phone spec wireframe. Header, two message rows, spec overview, next steps, source cards, quick actions, and composer are visible in one scroll state.
- `ScreenshotPhoneCodeReview`: aligns with the phone code-review wireframe after density fixes. Plan, diff, relevant files, ready-for-review card edge, quick actions, and composer are visible above the fold.
- `ScreenshotPhoneHandoff`: aligns with the phone handoff wireframe. Handoff illustration, primary desktop action, summary timeline, attachment chip, and composer match the intended stacked mobile scene.
- `ScreenshotPhoneArtifact`: aligns with the phone artifact wireframe. Dark NeuroCards preview, answer rows, open/iterate actions, and bottom composer retain the Cursor/Claude-like shell.
- `ScreenshotPhoneWriting`: aligns with the phone writing wireframe. Tone chips, refined draft card, why-this-works card, quick actions, and composer use the shared card/chip system.
- `ScreenshotFoldSpec`: aligns with the foldable spec wireframe. Conversation stays left, workbench stays right, and the persistent composer spans the bottom.
- `ScreenshotFoldCodeReview`: aligns with the foldable code-review wireframe. Plan, diff, relevant files, and ready card fit in the right pane with the 38/62 scene split.
- `ScreenshotFoldHandoff`: aligns with the foldable handoff wireframe. Desktop handoff card and summary timeline stay in the right pane while chat context remains left.
- `ScreenshotFoldArtifact`: aligns with the foldable artifact wireframe. Dark artifact preview anchors the right pane and keeps action controls below it.
- `ScreenshotFoldWriting`: aligns with the foldable writing wireframe. Tone chips sit above the draft card and the right pane preserves the reusable workbench rhythm.

## Component Checks

- `CursorHeader`: compact Cursor cube, wordmark, history icon, overflow icon, workspace chip, and model chip match the small Android-native header density in the wireframes.
- `ChatPane`: user bubble, assistant open text, avatars, timestamps, and quick-action chips match the Claude-like mobile conversation pattern without desktop chrome.
- `ComposerDock`: PromptBar-derived state model, attachment chips, text field, add/file/mic/send controls, and compact dock shape match the bottom composer in phone and foldable scenes.
- `WorkbenchCard`: shared cream surface, thin border, compact title row, icon slot, trailing slot, and reduced padding match the reusable design-system requirement.
- `CursorChip` and `StatusPill`: compact rounded chips cover quick actions, model/workspace selectors, tone selectors, status pills, and workbench actions.
- `Checklist`: circular completed/incomplete markers align with plan, next-step, and handoff summary rows.
- `SpecCards`: overview, next steps, and source-card rows are built from shared workbench primitives and match the product-spec state.
- `CodeDiffCard`: plan, diff, relevant files, and ready-for-review card are built from shared cards; diff rows were compacted to match the first-viewport wireframe.
- `HandoffCard`: desktop transfer illustration, primary dark CTA, resend action, and summary checklist match the handoff state while using shared tokens.
- `ArtifactPreview`: dark in-app preview, progress bar, answer rows, selected answer treatment, and artifact actions match the app-preview state.
- `DraftCard`: tone chips, refined draft surface, collapsible-style title row, and rationale card match the writing state.
- `CursorTwoPaneLayout`: foldable scene uses the required left conversation/right workbench split with a crease divider and persistent bottom composer.
- `CursorBackMorphProvider` and `cursorBackMorph`: predictive-back progress is exposed as a reusable motion primitive so messages, cards, source cards, and composer close independently in two dimensions.
- `CursorHaptics`: live agent responses use a soft app-specific vibration curve; predictive-back start/cancel/commit use stronger custom haptics.
