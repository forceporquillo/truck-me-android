package dev.forcecodes.truckme.extensions

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.SupportMapFragment
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.binding.FragmentViewBindingDelegate
import dev.forcecodes.truckme.ui.jobs.ActiveJobsActivity
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

fun Fragment.observeOnLifecycleStarted(
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
  (requireActivity() as AppCompatActivity).toast(message)
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

fun Fragment.startRealtimeMap(activeJobId: String) {
  requireActivity {
    val intent = Intent(this, ActiveJobsActivity::class.java)
    intent.putExtra("job_item_id", activeJobId)
    startActivity(intent)
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
const val ANIMATION_FAST_MILLIS_V2 = 50L

fun Fragment.postRunnable(block: () -> Unit) {
  view?.postKt(block)
}

fun Fragment.getDrawable(@DrawableRes drawableId: Int): Drawable? {
  return ContextCompat.getDrawable(requireContext(), drawableId)
}

fun Fragment.navigateUp(delay: Long = ANIMATION_FAST_MILLIS) {
  observeOnLifecycleStarted {
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

fun Fragment.navigate(direction: NavDirections) {
  findNavController().navigate(direction)
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