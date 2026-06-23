package com.example.cursor.data

import com.example.cursor.model.AgentStatus
import com.example.cursor.model.DiffDelta
import com.example.cursor.model.FabricPacket
import com.example.cursor.model.FabricPayload

internal fun seedFabricPackets(): List<FabricPacket> =
  listOf(
    FabricPacket(
      packetId = "packet-35-status-composer-alpha",
      sequenceNumber = 35,
      timestampMs = 1_719_145_000_035,
      hostId = "ryans-macbook-pro",
      workspaceId = "acme/mobile",
      agentRunId = "composer-alpha",
      payload = FabricPayload.StatusChanged(AgentStatus.ExecutingTool, "gradle test"),
    ),
    FabricPacket(
      packetId = "packet-36-token-composer-alpha",
      sequenceNumber = 36,
      timestampMs = 1_719_145_000_036,
      hostId = "ryans-macbook-pro",
      workspaceId = "acme/mobile",
      agentRunId = "composer-alpha",
      payload = FabricPayload.TokenChunk("Building Cursor shell and verifying render models..."),
    ),
    FabricPacket(
      packetId = "packet-37-diff-nav-display",
      sequenceNumber = 37,
      timestampMs = 1_719_145_000_037,
      hostId = "ryans-macbook-pro",
      workspaceId = "acme/mobile",
      agentRunId = "composer-alpha",
      payload =
        FabricPayload.DiffChanged(
          DiffDelta(
            filePath = "CursorNavDisplay.kt",
            lineStart = 30,
            hunks = listOf("@@ navigation scene @@"),
          )
        ),
    ),
    FabricPacket(
      packetId = "packet-38-diff-code-card",
      sequenceNumber = 38,
      timestampMs = 1_719_145_000_038,
      hostId = "ryans-macbook-pro",
      workspaceId = "acme/mobile",
      agentRunId = "composer-alpha",
      payload =
        FabricPayload.DiffChanged(
          DiffDelta(
            filePath = "CodeDiffCard.kt",
            lineStart = 18,
            hunks = listOf("@@ render rows @@"),
          )
        ),
    ),
    FabricPacket(
      packetId = "packet-39-design-system-thinking",
      sequenceNumber = 39,
      timestampMs = 1_719_145_000_039,
      hostId = "cursor-cloud-worker",
      workspaceId = "shape-prototype",
      agentRunId = "design-system",
      payload = FabricPayload.StatusChanged(AgentStatus.Thinking, null),
    ),
    FabricPacket(
      packetId = "packet-40-design-system-token",
      sequenceNumber = 40,
      timestampMs = 1_719_145_000_040,
      hostId = "cursor-cloud-worker",
      workspaceId = "shape-prototype",
      agentRunId = "design-system",
      payload = FabricPayload.TokenChunk("Preparing reusable components for foldable scenes."),
    ),
    FabricPacket(
      packetId = "packet-41-design-system-components",
      sequenceNumber = 41,
      timestampMs = 1_719_145_000_041,
      hostId = "cursor-cloud-worker",
      workspaceId = "shape-prototype",
      agentRunId = "design-system",
      payload =
        FabricPayload.DiffChanged(
          DiffDelta(
            filePath = "ComposerDock.kt",
            lineStart = 1,
            hunks = listOf("@@ component polish @@"),
          )
        ),
    ),
    FabricPacket(
      packetId = "packet-42-design-system-workbench",
      sequenceNumber = 42,
      timestampMs = 1_719_145_000_042,
      hostId = "cursor-cloud-worker",
      workspaceId = "shape-prototype",
      agentRunId = "design-system",
      payload =
        FabricPayload.DiffChanged(
          DiffDelta(
            filePath = "WorkbenchCards.kt",
            lineStart = 1,
            hunks = listOf("@@ topology strip @@"),
          )
        ),
    ),
  )
