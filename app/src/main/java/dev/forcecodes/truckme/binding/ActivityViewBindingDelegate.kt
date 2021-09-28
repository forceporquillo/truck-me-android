package dev.forcecodes.truckme.binding

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityViewBindingDelegate<out T : ViewBinding>(
  private val activity: AppCompatActivity,
  private val viewBindingFactory: (LayoutInflater) -> T
) : ReadOnlyProperty<AppCompatActivity, T>, LifecycleEventObserver {
  private var binding: T? = null

  init {
    activity.lifecycle.addObserver(this)
  }

  override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
    val binding = binding
    if (binding != null) {
      return binding
    }

    val lifecycle = activity.lifecycle
    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
      throw IllegalStateException("Should not attempt to get bindings when Activity views are destroyed.")
    }

    return viewBindingFactory(thisRef.layoutInflater).also {
      this@ActivityViewBindingDelegate.binding = it
    }
  }

  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    if (event == Lifecycle.Event.ON_DESTROY) {
      binding = null
      activity.lifecycle.removeObserver(this)
    }
  }
}

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
  noinline bindingInflater: (LayoutInflater) -> T
) = ActivityViewBindingDelegate(this, bindingInflater)