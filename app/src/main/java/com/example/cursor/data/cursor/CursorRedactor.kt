package com.example.cursor.data.cursor

object CursorRedactor {
  private val bearerPattern = Regex("Bearer\\s+[A-Za-z0-9._~+/-]+=*", RegexOption.IGNORE_CASE)
  private val tokenPattern = Regex("(api[_-]?key|token|authorization)\\s*[:=]\\s*[^\\s,}]+", RegexOption.IGNORE_CASE)

  fun redact(value: String): String =
    value
      .replace(bearerPattern, "Bearer [redacted]")
      .replace(tokenPattern) { match -> "${match.groupValues[1]}=[redacted]" }
}
