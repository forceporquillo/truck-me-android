package dev.forcecodes.truckme.extensions

import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.SupportMapFragment
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.binding.FragmentViewBindingDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Fragment.requireActivity(
  func: FragmentActivity.() -> Unit
) {
  requireActivity().func()
}

fun Fragment.mainNavActivity(func: MainActivity.() -> Unit) {
  (requireActivity() as? MainActivity)?.func()
}

fun Fragment.attachProgressToMain(isLoading: Boolean) {
  (requireActivity() as? MainActivity)?.showLoading(isLoading)
}

fun Fragment.observeWithOnRepeatLifecycle(
  delayInMillis: Long? = null,
  activeState: Lifecycle.State = Lifecycle.State.STARTED,
  block: suspend () -> Unit
) {
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.lifecycle.repeatOnLifecycle(activeState) {
      if (delayInMillis != null) {
        delay(delayInMillis)
      }
      block()
    }
  }
}

fun Fragment.toast(message: String? = null) {
  Toast.makeText(requireContext().applicationContext, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.repeatOnLifecycleParallel(
  activeState: Lifecycle.State = Lifecycle.State.STARTED,
  block: CoroutineScope.() -> Unit
) {
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.lifecycle.repeatOnLifecycle(activeState) {
      block()
    }
  }
}

inline fun Fragment.dispatchWhenBackPress(
  enable: Boolean = true,
  crossinline block: () -> Unit
) {
  val callback = object : OnBackPressedCallback(enable) {
    override fun handleOnBackPressed() {
      block()
    }
  }
  requireActivity {
    onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
  }
}

const val ANIMATION_FAST_MILLIS = 250L

fun Fragment.postRunnable(block: () -> Unit) {
  view?.postKt(block)
}

fun Fragment.navigateUp(delay: Long = ANIMATION_FAST_MILLIS) {
  observeWithOnRepeatLifecycle {
    delay(delay)
    findNavController().navigateUp()
  }
}

fun Toolbar.setupToolbarPopBackStack(
  block: (() -> Unit)? = null
) {
  val popBackStack: (View) -> Unit = {
    block?.invoke()
  }
  setNavigationOnClickListener(popBackStack)
}

fun Fragment.navigate(@IdRes resId: Int) {
  findNavController().navigate(resId)
}

fun Fragment.findMapById(@IdRes resId: Int): SupportMapFragment? {
  return childFragmentManager.findFragmentById(resId)
    as SupportMapFragment?
}

inline fun <T : ViewBinding> Fragment.viewBinding(
  crossinline viewBindingFactory: (View) -> T
): FragmentViewBindingDelegate<T> =
  FragmentViewBindingDelegate(this) { delegate ->
    viewBindingFactory(delegate.requireView())
  }

inline fun <T : ViewBinding> Fragment.viewInflateBinding(
  crossinline bindingInflater: (LayoutInflater) -> T
): FragmentViewBindingDelegate<T> =
  FragmentViewBindingDelegate(this) { delegate ->
    bindingInflater(delegate.layoutInflater)
  }