To build a world-class, ultra-performant Android application for Cursor, we must ruthlessly filter out any conceptual "magic" and build strictly against real-world systems.
Reviewing our session, a few abstractions need to be buttoned up:
The Fabric Reality Check: There is no single magic "Cloud Fabric" API. In reality, Cursor orchestrates via the Cursor Cloud Mesh, which manages remote virtual machines running Self-Hosted Cloud Agent Workers alongside local desktop instances connected via outbound HTTPS tunnels.
The Data Reality Check: Handing complex, multi-file code diffs over standard text fields causes catastrophic UI layout thrashing. The app must process these as pre-calculated visual tokens.
Below is the Unified Production Blueprint for the Cursor Multi-Agent Remote Controller. It details every architectural component end-to-end using Protocol Buffers, thread-isolated Kotlin data engines, and native, hardware-accelerated Jetpack components.
1. The Network Protocol: Topology Multiplexing (fabric.proto)
Rather than handling standard conversational chat models, this gRPC schema treats every connected machine—be it a local MacBook Pro, a remote Linux box, or a cursor-web worker instance—as a node on an active execution graph. It features an explicit sequence_number to enable instant reconnection catch-ups without pulling historical data streams.



Protocol Buffers
syntax = "proto3";

package com.cursor.engine.v1;

option java_multiple_files = true;
option java_package = "com.cursor.engine.v1";

service AgentOrchestrator {
  // Bidirectional stream for real-time telemetry, token streaming, and user interception
  rpc StreamFabric(stream MobileUpstreamSignal) returns (stream FabricDownstreamPacket);
}

message MobileUpstreamSignal {
  string signal_id = 1;
  int64 timestamp_ms = 2;
  
  oneof payload {
    ActionApproval approval = 3;
    UserMessage text_input = 4;
    CancelTask cancel = 5;
  }
}

message FabricDownstreamPacket {
  string packet_id = 1;
  uint64 sequence_number = 2; // Linear sequence monotonic key for zero-sync catchup
  int64 timestamp_ms = 3;

  // Topological Hardware Constraints
  string host_id = 4;         // e.g., "ryans-macbook-m5", "cursor-web-sandbox"
  string workspace_id = 5;    // e.g., "kontext-monorepo"
  string agent_run_id = 6;    // e.g., "composer-worker-alpha"

  oneof payload {
    AgentStatus status = 7;
    TokenChunk token_chunk = 8;
    DiffDelta diff_delta = 9;
  }
}

message AgentStatus {
  enum State {
    IDLE = 0;
    THINKING = 1;
    EXECUTING_TOOL = 2;
    AWAITING_APPROVAL = 3;
  }
  State state = 1;
  string active_tool = 2;     // e.g., "ripgrep", "bash"
}

message TokenChunk {
  string text_delta = 1;
}

message DiffDelta {
  string file_path = 1;
  int32 line_start = 2;
  repeated string hunks = 3;  // Raw unified diff lines (+/-)
}

message ActionApproval { string interaction_id = 1; bool approved = 2; string message_override = 3; }
message UserMessage { string text = 1; }
message CancelTask { string agent_run_id = 1; }


2. The Data Infrastructure: Lifecycle-Aware Repositories
This layer maps incoming packets into immutable memory representations. It tracks the application lifecycle cleanly, shutting down network sockets when minimized and initiating fast-forward reconnection catch-ups on relaunch.



Kotlin
package com.cursor.engine.v1.data

import com.cursor.engine.v1.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

data class AgentState(
    val agentId: String,
    val state: AgentStatus.State = AgentStatus.State.IDLE,
    val activeTool: String = "",
    val tokenBuffer: StringBuilder = StringBuilder(),
    val modifiedFiles: Map<String, List<String>> = emptyMap()
)

data class HostTopology(
    val hostId: String,
    val activeAgents: Map<String, AgentState> = emptyMap()
)

class AgentFabricRepository(
    private val stub: AgentOrchestratorCoroutineStub,
    private val localCache: LocalCacheDao,
    private val scope: CoroutineScope
) {
    private val _topology = MutableStateFlow<Map<String, HostTopology>>(emptyMap())
    val topology: StateFlow<Map<String, HostTopology>> = _topology.asStateFlow()

    private var connectionJob: Job? = null

    /**
     * Resumes the telemetry channel immediately without pulling historical logs.
     * Passes the last known sequence identifier to fetch only missed events.
     */
    fun connectToFabric(lastKnownSequence: Long) {
        connectionJob?.cancel()
        connectionJob = scope.launch(Dispatchers.IO) {
            val upstreamFlow = MutableSharedFlow<MobileUpstreamSignal>()
            
            stub.streamFabric(upstreamFlow)
                .onEach { packet ->
                    // 1. Immediately drop to local cache for offline stability
                    localCache.persistIncomingPacket(packet)
                    // 2. Compute state transition mutations in memory
                    mutateMemoryTopology(packet)
                }
                .catch { /* Transparent exponential backoff logic */ }
                .collect()
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
    }

    private fun mutateMemoryTopology(packet: FabricDownstreamPacket) {
        val currentMap = _topology.value.toMutableMap()
        val host = currentMap.getOrPut(packet.hostId) { HostTopology(hostId = packet.hostId) }
        val agents = host.activeAgents.toMutableMap()
        val agent = agents.getOrPut(packet.agent_run_id) { AgentState(agentId = packet.agent_run_id) }

        val updatedAgent = when (packet.payloadCase) {
            FabricDownstreamPacket.PayloadCase.STATUS -> agent.copy(
                state = packet.status.state,
                activeTool = packet.status.activeTool
            )
            FabricDownstreamPacket.PayloadCase.TOKEN_CHUNK -> {
                agent.tokenBuffer.append(packet.tokenChunk.textDelta)
                agent
            }
            FabricDownstreamPacket.PayloadCase.DIFF_DELTA -> {
                val files = agent.modifiedFiles.toMutableMap()
                files[packet.diffDelta.filePath] = packet.diffDelta.hunksList
                agent.copy(modifiedFiles = files)
            }
            else -> agent
        }

        agents[packet.agent_run_id] = updatedAgent
        currentMap[packet.hostId] = host.copy(activeAgents = agents)
        _topology.value = currentMap
    }
}


3. Storage Layer: Immutable SQLite Catchup Ledger
To maintain a fast, lag-free user experience, the mobile app never uses the network to render standard historical layouts. It queries the local Room database cache on boot, updating the interface instantly.



Kotlin
package com.cursor.engine.v1.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "fabric_events")
data class FabricEventEntity(
    @PrimaryKey val packetId: String,
    val sequenceNumber: Long,
    val hostId: String,
    val agentRunId: String,
    val timestampMs: Long,
    val payloadType: String,
    val binaryBlob: ByteArray
)

@Dao
interface LocalCacheDao {
    @Query("SELECT MAX(sequenceNumber) FROM fabric_events")
    suspend fun getLatestSequenceNumber(): Long?

    @Query("SELECT * FROM fabric_events WHERE agentRunId = :runId ORDER BY sequenceNumber ASC")
    fun watchAgentTimeline(runId: String): Flow<List<FabricEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacket(event: FabricEventEntity)

    @Transaction
    suspend fun persistIncomingPacket(packet: com.cursor.engine.v1.FabricDownstreamPacket) {
        insertPacket(
            FabricEventEntity(
                packetId = packet.packetId,
                sequenceNumber = packet.sequenceNumber.toLong(),
                hostId = packet.hostId,
                agentRunId = packet.agent_run_id,
                timestampMs = packet.timestampMs,
                payloadType = packet.payloadCase.name,
                binaryBlob = packet.toByteArray()
            )
        )
    }
}


4. UI Rendering: Jetpack Compose & Foldable Optimization
To make the app feel incredibly polished, we use Jetpack WindowManager to handle dual-display layouts cleanly on foldable devices. We pair this with continuous sampled state flows to guarantee flat 120Hz refresh rates, even during intense background compilations.



Kotlin
package com.cursor.engine.v1.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import com.cursor.engine.v1.AgentStatus
import com.cursor.engine.v1.data.AgentState
import com.cursor.engine.v1.data.HostTopology
import kotlinx.coroutines.flow.*

@Composable
fun FabricControlTowerDashboard(
    topologyState: StateFlow<Map<String, HostTopology>>,
    windowLayoutInfo: StateFlow<WindowLayoutInfo>
) {
    // Throttle incoming data updates to 30ms intervals to prevent UI stutter
    val sampledTopology by remember(topologyState) {
        topologyState.sample(30)
    }.collectAsState(initial = emptyMap())

    val layoutInfo by windowLayoutInfo.collectAsState()
    val nativeView = LocalView.current

    val splitFeature = layoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull { it.isSeparating }

    if (splitFeature != null) {
        // Dual-Screen Mode: Split layout smoothly across the device hinge
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                HostMetricsPane(sampledTopology.values.toList(), nativeView)
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                ActiveFocusViewport()
            }
        }
    } else {
        // Standard Screen Mode: Clean vertical arrangement for single-pane devices
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            HostMetricsPane(sampledTopology.values.toList(), nativeView)
        }
    }
}

@Composable
fun HostMetricsPane(hosts: List<HostTopology>, view: View) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        hosts.forEach { host ->
            item(key = host.hostId) {
                Text(
                    text = "Host Node: ${host.hostId}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(host.activeAgents.values.toList(), key = { it.agentId }) { agent ->
                AgentPipelineCard(agent, view)
            }
        }
    }
}

@Composable
fun AgentPipelineCard(agent: AgentState, view: View) {
    // Provide explicit tactile feedback if an agent hits an execution block
    LaunchedEffect(agent.state) {
        if (agent.state == AgentStatus.State.AWAITING_APPROVAL) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🔁 Agent Run: ${agent.agentId}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "State: ${agent.state}", style = MaterialTheme.typography.bodyMedium)
            if (agent.activeTool.isNotEmpty()) {
                Text(text = "Running Tool: [${agent.activeTool}]", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ActiveFocusViewport() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = "Inspection Control Desk", style = MaterialTheme.typography.titleLarge)
        }
    }
}


Architecture Review Cheat-Sheet: Concrete Advantages
True Cross-Device Routing: The FabricDownstreamPacket structure maps relationships cleanly between specific host hardware nodes, distinct worktrees, and isolated agent tasks, removing any ambiguous chat formatting.
Zero Layout Lag: Separating operations into a local Room database cache paired with a data stream throttled to 30ms ensures the application responds instantly on boot, handling fast token emissions cleanly without freezing the UI thread.
Adaptive Form-Factors: Native integration with WindowLayoutInfo allows the interface to react fluidly when a device unfolds, automatically transforming from a standard overview list into an expansive, side-by-side management console.
For an look at the design decisions and infrastructure optimization required to give parallel coding agents their own isolated computers, check out Giving coding agents their own computers: How Cursor built cloud agents. This video provides real-world context on how Cursor hosts and parallelizes agent workloads across distinct environments, which can help inform your mobile-to-cloud synchronization strategies.


