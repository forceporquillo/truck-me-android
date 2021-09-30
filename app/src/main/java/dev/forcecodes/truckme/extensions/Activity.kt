package dev.forcecodes.truckme.extensions

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlin.reflect.KClass

fun AppCompatActivity.fillDecor(toolbar: Toolbar) {
  setSupportActionBar(toolbar)
  supportActionBar?.setDisplayHomeAsUpEnabled(true)
  toolbar.setNavigationOnClickListener { finish() }
}

fun <T : AppCompatActivity> Activity.createIntent(
  activity: KClass<out T>,
  intent: Intent = Intent(this, activity.java),
  finish: Boolean = false,
  finishAffinity: Boolean = false,
) {
  startActivity(intent)
  if (finish) {
    finish()
    return
  }

  if (finishAffinity) {
    finishAffinity()
  }
}