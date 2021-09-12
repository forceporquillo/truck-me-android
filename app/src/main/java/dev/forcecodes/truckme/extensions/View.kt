package dev.forcecodes.truckme.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

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
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {}
}

fun AppCompatEditText.textChangeObserver(block: (String?) -> Unit) {
    addTextChangedListener(object : TextChangeListener() {
        override fun afterTextChanged(s: Editable?) {
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
