package dev.forcecodes.truckme.extensions

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.util.then
import dev.forcecodes.truckme.util.GlideApp
import java.io.ByteArrayOutputStream

fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, ViewPaddingState) -> Unit) {
  // Create a snapshot of the view's padding state
  val paddingState = createStateForView(this)
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    f(v, insets, paddingState)
    insets
  }
  requestApplyInsetsWhenAttached()
}

/**
 * Call [View.requestApplyInsets] in a safe away. If we're attached it calls it straight-away.
 * If not it sets an [View.OnAttachStateChangeListener] and waits to be attached before calling
 * [View.requestApplyInsets].
 */

fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    requestApplyInsets()
  } else {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}

private fun createStateForView(view: View) = ViewPaddingState(
  view.paddingLeft,
  view.paddingTop,
  view.paddingRight,
  view.paddingBottom,
  view.paddingStart,
  view.paddingEnd
)

data class ViewPaddingState(
  val left: Int,
  val top: Int,
  val right: Int,
  val bottom: Int,
  val start: Int,
  val end: Int
)

fun Toolbar.setUpGradientToolbar() {
  doOnApplyWindowInsets { view, windowInsetsCompat, viewPaddingState ->
    val statusBar = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars())
    view.updatePadding(top = viewPaddingState.top + statusBar.top)
  }
}

private abstract class TextChangeListener : TextWatcher {
  override fun beforeTextChanged(
    s: CharSequence?,
    start: Int,
    count: Int,
    after: Int
  ) {
  }

  override fun onTextChanged(
    s: CharSequence?,
    start: Int,
    before: Int,
    count: Int
  ) {
  }

  override fun afterTextChanged(s: Editable?) {}
}

fun AppCompatEditText.textChangeObserver(block: (String) -> Unit) {
  addTextChangedListener(object : TextChangeListener() {
    override fun afterTextChanged(s: Editable?) {
      val inputText = s.toString()
      block(inputText)
    }
  })
}

fun AutoCompleteTextView.textChangeObserver(block: (String) -> Unit) {
  addTextChangedListener(object : TextChangeListener() {
    override fun afterTextChanged(s: Editable?) {
      super.afterTextChanged(s)
      val inputText = s.toString()
      block(inputText)
    }
  })
}

inline fun View.updateMarginParams(block: ViewGroup.MarginLayoutParams.() -> Unit) {
  val params = layoutParams as ViewGroup.MarginLayoutParams
  block(params)
  layoutParams = params
}

@Suppress("Deprecation")
fun ImageView.compressAsBitmap(): ByteArray {
  isDrawingCacheEnabled = true
  buildDrawingCache()

  val bitmap = (drawable as? BitmapDrawable)?.bitmap
  val baos = ByteArrayOutputStream()
  bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

  return baos.toByteArray()
}

inline fun <T> ImageView.bindProfileIcon(
  data: T,
  crossinline block: (Boolean) -> Unit
) {
  bindImageWith(this, data) { result ->
    block(result)
  }
}

fun <T> bindImageWith(
  view: ImageView,
  data: T?,
  block: ((Boolean) -> Unit)? = null
) {
  GlideApp.with(view.context)
    .asBitmap()
    .override(view.width, view.height)
    .load(data)
    .listener(object : RequestListener<Bitmap> {
      override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Bitmap>?,
        isFirstResource: Boolean
      ): Boolean {
        block?.invoke(false)
        return false
      }

      override fun onResourceReady(
        resource: Bitmap?,
        model: Any?,
        target: Target<Bitmap>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
      ): Boolean {
        block?.invoke(true)
        return false
      }
    })
    .into(view)
}

fun View.disableButtonForAWhile(block: () -> Unit) {
  isEnabled = false
  block()
  handler.postDelayed({
    isEnabled = true
  }, 500L)
}

/**
 * Listens to scroll offset position of [RecyclerView] and add [Toolbar] elevation if
 * first visible item is not present in the view hierarchy.
 */
fun <T: View> NestedScrollView.withToolbarElevationListener(
  view: T,
  block: (() -> Unit)? = null
) {
  setOnScrollChangeListener { _, _, _, _, _ ->
    view.elevation = if (!canScrollVertically(-1)) 0f else 4f
    block?.invoke()
  }
}

fun View.postKt(block: () -> Unit) {
  this.post { block() }
}

fun ImageView.setActiveStateIndicatorColor(isActive: Boolean) {
  val colorId = isActive then R.color.active ?: R.color.inactive
  setColorFilter(ContextCompat.getColor(context, colorId))
}