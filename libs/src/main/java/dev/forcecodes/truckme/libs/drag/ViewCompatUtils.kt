
package dev.forcecodes.truckme.libs.drag

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

fun setControlFocusInsetsAnimation(view: View) {
    val controlFocus = ControlFocusInsetsAnimationCallback(view)
    ViewCompat.setWindowInsetsAnimationCallback(view, controlFocus)
}

fun setWindowInsetsAnimationCallback(
    view: View,
    flag: Int = 1
) {
    val dispatchMode = when (flag) {
        0 -> WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
        1 -> WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
        else -> throw IllegalStateException()
    }

    ViewCompat.setWindowInsetsAnimationCallback(
        view,
        TranslateDeferringInsetsAnimationCallback(
            view = view,
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime(),
            dispatchMode = dispatchMode
        )
    )
}