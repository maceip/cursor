package com.example.cursor.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val CursorSceneDurationMs = 320
private val CursorEase = CubicBezierEasing(0.2f, 0f, 0f, 1f)

fun cursorSceneSizeTransform(): SizeTransform =
  SizeTransform(clip = false) { _, _ -> tween(CursorSceneDurationMs, easing = CursorEase) }

fun AnimatedContentTransitionScope<Scene<NavKey>>.cursorSceneTransition(): ContentTransform =
  (fadeIn(tween(CursorSceneDurationMs, easing = CursorEase)) +
    scaleIn(tween(CursorSceneDurationMs, easing = CursorEase), initialScale = 0.985f))
    .togetherWith(
      fadeOut(tween(CursorSceneDurationMs, easing = CursorEase)) +
        scaleOut(tween(CursorSceneDurationMs, easing = CursorEase), targetScale = 1.015f)
    )

fun AnimatedContentTransitionScope<Scene<NavKey>>.cursorPopSceneTransition(): ContentTransform =
  (fadeIn(tween(CursorSceneDurationMs, easing = CursorEase)) +
    scaleIn(tween(CursorSceneDurationMs, easing = CursorEase), initialScale = 1.02f))
    .togetherWith(
      fadeOut(tween(CursorSceneDurationMs, easing = CursorEase)) +
        scaleOut(tween(CursorSceneDurationMs, easing = CursorEase), targetScale = 0.94f)
    )

fun AnimatedContentTransitionScope<Scene<NavKey>>.cursorPredictivePopSceneTransition(progress: Int): ContentTransform {
  val targetScale = 0.92f + (progress.coerceIn(0, 100) / 100f) * 0.02f
  return (fadeIn(tween(CursorSceneDurationMs, easing = CursorEase)) +
    scaleIn(tween(CursorSceneDurationMs, easing = CursorEase), initialScale = 1.02f))
    .togetherWith(
      fadeOut(tween(CursorSceneDurationMs, easing = CursorEase)) +
        scaleOut(tween(CursorSceneDurationMs, easing = CursorEase), targetScale = targetScale)
    )
}
