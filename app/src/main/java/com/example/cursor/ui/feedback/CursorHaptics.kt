package com.example.cursor.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode

class CursorHaptics internal constructor(
  private val vibrator: Vibrator?,
  private val fallback: HapticFeedback,
  private val enabled: Boolean,
) {
  fun liveAgentResponse() {
    if (!enabled) return
    vibrate(
      timings = longArrayOf(0, 9, 28, 13, 46, 8),
      amplitudes = intArrayOf(0, 42, 0, 64, 0, 35),
      fallbackType = HapticFeedbackType.TextHandleMove,
    )
  }

  fun predictiveBackStart() {
    if (!enabled) return
    vibrate(
      timings = longArrayOf(0, 8, 32, 10),
      amplitudes = intArrayOf(0, 76, 0, 48),
      fallbackType = HapticFeedbackType.LongPress,
    )
  }

  fun predictiveBackCommit() {
    if (!enabled) return
    vibrate(
      timings = longArrayOf(0, 18, 18, 28),
      amplitudes = intArrayOf(0, 145, 0, 215),
      fallbackType = HapticFeedbackType.LongPress,
    )
  }

  fun predictiveBackCancel() {
    if (!enabled) return
    vibrate(
      timings = longArrayOf(0, 7, 22, 7),
      amplitudes = intArrayOf(0, 58, 0, 38),
      fallbackType = HapticFeedbackType.TextHandleMove,
    )
  }

  private fun vibrate(
    timings: LongArray,
    amplitudes: IntArray,
    fallbackType: HapticFeedbackType,
  ) {
    val target = vibrator?.takeIf { it.hasVibrator() }
    if (target == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      fallback.performHapticFeedback(fallbackType)
      return
    }
    target.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
  }
}

@Composable
fun rememberCursorHaptics(): CursorHaptics {
  val context = LocalContext.current
  val fallback = LocalHapticFeedback.current
  val enabled = !LocalInspectionMode.current
  return remember(context, fallback, enabled) {
    CursorHaptics(vibrator = context.cursorVibrator(), fallback = fallback, enabled = enabled)
  }
}

private fun Context.cursorVibrator(): Vibrator? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    getSystemService(VibratorManager::class.java)?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }
