package com.example.cursor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.window.layout.WindowInfoTracker
import com.example.cursor.ui.theme.CursorClaudeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    val windowLayoutInfo =
      WindowInfoTracker
        .getOrCreate(this)
        .windowLayoutInfo(this)

    setContent {
      CursorClaudeTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(windowLayoutInfo = windowLayoutInfo)
        }
      }
    }
  }
}
