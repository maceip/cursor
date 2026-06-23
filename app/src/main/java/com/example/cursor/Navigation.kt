package com.example.cursor

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cursor.nav.CursorNavDisplay

@Composable
fun MainNavigation() {
  CursorNavDisplay(modifier = Modifier.safeDrawingPadding())
}
