package dev.forcecodes.truckme.core.util

import android.content.Context
import android.content.SharedPreferences

fun Context.storeAdminToken(token: String) {
  if (isDriver) {
    throw IllegalStateException("Only admin build can request this method.")
  }
  val prefs = sharedTokenPrefs()
  prefs.edit().putString("admin_token", token).apply()
}

fun Context.getAdminToken(): String? {
  if (isDriver) {
    throw IllegalStateException("Only admin build can request this method.")
  }
  return sharedTokenPrefs().getString("admin_token", null)
}

private fun Context.sharedTokenPrefs(): SharedPreferences {
  return getSharedPreferences("admin_token_prefs", Context.MODE_PRIVATE)
}