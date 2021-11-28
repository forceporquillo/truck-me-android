package dev.forcecodes.truckme.core.util

import android.content.Context
import android.content.SharedPreferences

fun Context.storeAdminToken(token: String) {
  val prefs = sharedTokenPrefs()
  prefs.edit().putString("admin_token", token).apply()
}

fun Context.getAdminToken(): String? {
  return sharedTokenPrefs().getString("admin_token", null)
}

private fun Context.sharedTokenPrefs(): SharedPreferences {
  if (isDriver) {
    throw IllegalStateException("Only admin build can request this method.")
  }
  return getSharedPreferences("admin_token_prefs", Context.MODE_PRIVATE)
}