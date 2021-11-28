package dev.forcecodes.truckme.extensions

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

fun AppCompatActivity.fillDecor(toolbar: Toolbar, finish: Boolean = false) {
  setSupportActionBar(toolbar)
  supportActionBar?.setDisplayHomeAsUpEnabled(true)
  if (finish) {
    toolbar.setNavigationOnClickListener { finish() }
  }
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

fun AppCompatActivity.onLifecycleStarted(
  block: suspend () -> Unit
) {
  lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
      block()
    }
  }
}

fun AppCompatActivity.toast(message: String? = null) {
  Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}