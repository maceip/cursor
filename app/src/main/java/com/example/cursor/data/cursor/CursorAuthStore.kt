package com.example.cursor.data.cursor

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CursorAuthStore(context: Context) {
  private val preferences: SharedPreferences =
    context.applicationContext.getSharedPreferences("cursor-auth", Context.MODE_PRIVATE)

  fun saveToken(kind: CursorAccountKind, token: String) {
    val encrypted = encrypt(token.trim())
    preferences.edit().putString(kind.preferenceName, encrypted).apply()
  }

  fun token(kind: CursorAccountKind): String? =
    preferences.getString(kind.preferenceName, null)?.let(::decrypt)?.takeIf { it.isNotBlank() }

  fun clearToken(kind: CursorAccountKind) {
    preferences.edit().remove(kind.preferenceName).apply()
  }

  private fun encrypt(value: String): String {
    val cipher = Cipher.getInstance(Transformation)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey())
    val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    return "${Base64.encodeToString(cipher.iv, Base64.NO_WRAP)}:${Base64.encodeToString(ciphertext, Base64.NO_WRAP)}"
  }

  private fun decrypt(value: String): String? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    return runCatching {
        val cipher = Cipher.getInstance(Transformation)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(ciphertext), Charsets.UTF_8)
      }
      .getOrNull()
  }

  private fun secretKey(): SecretKey {
    val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
    (keyStore.getEntry(KeyAlias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
    val spec =
      KeyGenParameterSpec.Builder(KeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()
    generator.init(spec)
    return generator.generateKey()
  }

  private val CursorAccountKind.preferenceName: String
    get() = "cursor-api-token-${storageKey}"

  companion object {
    private const val AndroidKeyStore = "AndroidKeyStore"
    private const val KeyAlias = "cursor-api-token-key"
    private const val Transformation = "AES/GCM/NoPadding"
  }
}
