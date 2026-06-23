1. The Definitive Topology: Cloud-Orchestrated, Locally Augmented
By utilizing Self-Hosted Pools, the network topology shifts away from brittle client-side scraping. Instead, it forms a structured three-plane architecture:

                                  [ Cursor Cloud Control Plane ]
                                    (Central Orchestrator/API)
                                                ▲
                       ┌────────────────────────┴────────────────────────┐
          gRPC over TLS│                                                 │Outbound HTTPS Polling
                       ▼                                                 ▼
               [ Android Client ] <====== LAN / Google Nearby =====> [ Self-Hosted Worker Pool ]
             (Mobile Control Tower)         (Ultra-Low Latency)       (Your Hardware/VMs/Render)
The Outbound Worker Fleet: Your laptops, remote VMs, or Kubernetes clusters run agent worker start. They poll Cursor’s cloud control plane using outbound-only HTTPS requests—meaning you don't have to worry about inbound ports, firewalls, or complex VPN setups.

The Cloud State Ledger: When an agent task is triggered (via a PR, Slack, or the Cursor SDK), Cursor’s cloud infrastructure handles the core task planning. It streams distinct tool-calls down to your worker fleet while maintaining a clear history of logs, step updates, and user approval tokens in the cloud.

The Android Client: Your app connects directly to Cursor's public APIs using a high-performance gRPC bidirectional stream, serving as a remote dashboard for these active tasks.

2. Managing the Offline State Problem with Vector Clocks
Even with a cloud backend, your mobile device and your worker fleet can still drift if one goes offline while the other is executing tasks. To handle these synchronization changes smoothly without data loss, the architecture uses an append-only transaction ledger tracked by a Vector Clock Graph rather than a transient text stream.

The Replicated Event Protocol (cursor_sync.proto)
Protocol Buffers
syntax = "proto3";

package com.cursor.sync.v1;

option java_multiple_files = true;

message VectorClock {
  map<string, uint64> actor_versions = 1; // e.g., {"ryans-pixel-fold": 104, "office-macbook-worker": 892}
}

message TransactionMutation {
  string mutation_id = 1;
  VectorClock clock = 2;
  int64 timestamp_ms = 3;
  string task_id = 4;

  oneof data {
    StageTransition stage = 5;
    LogBlock logs = 6;
    HumanInterrupt interrupt = 7;
  }
}

message StageTransition { string stage_name = 1; string step_details = 2; }
message LogBlock { string ansi_text_delta = 1; }
message HumanInterrupt { string prompt = 1; repeated string options = 2; }

message SyncHandshake { VectorClock current_knowledge = 1; }
message MutationBatch { repeated TransactionMutation missing_mutations = 1; }
3. Storage Layer: Immutable SQLite Catchup Ledger
To ensure the application loads instantly when launching offline, the UI renders immediately from local storage. Incoming network deltas are resolved on a background thread and written directly to the database via Room transactions, updating the interface smoothly.

Kotlin
package com.cursor.sync.v1.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pool_tasks")
data class PoolTaskEntity(
    @PrimaryKey val taskId: String,
    val currentStage: String,
    val stepDetails: String,
    val lastKnownSequence: Long
)

@Entity(tableName = "pool_mutations")
data class MutationEntity(
    @PrimaryKey val mutationId: String,
    val taskId: String,
    val actorId: String,
    val sequenceNumber: Long,
    val timestampMs: Long,
    val rawPayload: ByteArray
)

@Dao
interface SyncLedgerDao {
    @Query("SELECT MAX(sequenceNumber) FROM pool_mutations WHERE actorId = :actorId")
    suspend fun getHighestSequenceForActor(actorId: String): Long?

    @Query("SELECT * FROM pool_tasks ORDER BY lastKnownSequence DESC")
    fun watchActiveTasks(): Flow<List<PoolTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskState(task: PoolTaskEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun logMutation(mutation: MutationEntity)
}
4. UI Layer: Jetpack Compose & Foldable Optimization
To handle heavy terminal log outputs cleanly without causing interface lag, screen updates are sampled using a 30ms time window. We pair this with Jetpack WindowManager to provide an optimized layout that automatically adapts to foldable hardware postures.

Kotlin
package com.cursor.sync.v1.ui

import android.view.HapticFeedbackConstants
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
import com.cursor.sync.v1.data.db.PoolTaskEntity
import kotlinx.coroutines.flow.*

@Composable
fun EnterprisePoolDashboard(
    taskFlow: Flow<List<PoolTaskEntity>>,
    windowLayoutInfo: StateFlow<WindowLayoutInfo>
) {
    // Throttle incoming updates to 30ms intervals to protect the UI thread from log spikes
    val tasks by remember(taskFlow) {
        taskFlow.sample(30)
    }.collectAsState(initial = emptyList())

    val layoutInfo by windowLayoutInfo.collectAsState()
    val nativeView = LocalView.current

    val splitHinge = layoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull { it.isSeparating }

    if (splitHinge != null) {
        // Dual-Screen Mode: Split layout smoothly across the display hinge
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp)) {
                TaskTelemetryList(tasks, nativeView)
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                DetailedInspectionPane()
            }
        }
    } else {
        // Standard Screen Mode: Balanced single-column arrangement
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TaskTelemetryList(tasks, nativeView)
        }
    }
}

@Composable
fun TaskTelemetryList(tasks: List<PoolTaskEntity>, view: android.view.View) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tasks, key = { it.taskId }) { task ->
            // Trigger a physical haptic pulse if a background worker requires user review
            LaunchedEffect(task.currentStage) {
                if (task.currentStage == "AWAITING_HUMAN_INTERRUPT") {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "🛡️ Pool Task: ${task.taskId.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Stage: ${task.currentStage}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = task.stepDetails, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun DetailedInspectionPane() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = "Inspection Console Desk", style = MaterialTheme.typography.titleMedium)
        }
    }
}
Technical Edge to Highlight During Your Architecture Review
Production-Ready Strategy: By syncing exclusively with Cloud Agents and registered Self-Hosted Pools via Cursor’s active API boundaries, you avoid fragile client-side tracking workarounds like custom terminal daemons or hidden WebViews.

Lag-Free Resumption: The application never displays slow loading states upon launching. It renders your workflow states instantly from local Room layers while resolving connection changes in the background via small gRPC deltas.

Deterministic Convergence: Tracking state changes as append-only mutations tied to vector clocks guarantees that the phone and the self-hosted worker fleet will always arrive at the exact same state history once they reconnect, regardless of network dropouts or lifecycle disruptions.
