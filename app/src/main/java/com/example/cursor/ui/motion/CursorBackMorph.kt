package com.example.cursor.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.pow

private val LocalBackMorphProgress = compositionLocalOf { 0f }

@Composable
fun CursorBackMorphProvider(
  progress: Float,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalBackMorphProgress provides progress.coerceIn(0f, 1f), content = content)
}

@Composable
fun Modifier.cursorBackMorph(
  scaleXTarget: Float = 0.94f,
  scaleYTarget: Float = 0.88f,
  alphaTarget: Float = 0.72f,
  translateY: Float = 18f,
  transformOrigin: TransformOrigin = TransformOrigin(0.5f, 0.5f),
): Modifier {
  val eased = LocalBackMorphProgress.current.easeOutCubic()
  if (eased <= 0f) return this
  return graphicsLayer {
    this.transformOrigin = transformOrigin
    scaleX = lerp(1f, scaleXTarget, eased)
    scaleY = lerp(1f, scaleYTarget, eased)
    alpha = lerp(1f, alphaTarget, eased)
    translationY = translateY * eased
  }
}

private fun Float.easeOutCubic(): Float = 1f - (1f - this).pow(3)

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
