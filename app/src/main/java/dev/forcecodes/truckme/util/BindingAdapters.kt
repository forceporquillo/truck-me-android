package dev.forcecodes.truckme.util

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.ui.auth.BaseAuthViewModel
import dev.forcecodes.truckme.ui.auth.CommonCredentialsViewModel
import dev.forcecodes.truckme.ui.gallery.Image
import kotlinx.coroutines.flow.StateFlow

@BindingAdapter("emailObserver")
fun AppCompatEditText.emailObserver(
    viewModel: BaseAuthViewModel<*>
) {
    textChangeObserver { viewModel.email = it }
}

@BindingAdapter("passwordObserver")
fun AppCompatEditText.passwordObserver(
    viewModel: BaseAuthViewModel<*>
) {
    textChangeObserver { viewModel.password = it }
}

@BindingAdapter("passwordResetObserver")
fun AppCompatEditText.passwordResetObserver(
    viewModel: BaseAuthViewModel<*>
) {
    textChangeObserver { viewModel.email = it }
}

@BindingAdapter("confirmPasswordObserver")
fun AppCompatEditText.confirmPasswordObserver(
    viewModel: CommonCredentialsViewModel<*>
) {
    textChangeObserver { viewModel.confirmPassword = it }
}

@BindingAdapter("contactNumberObserver")
fun AppCompatEditText.contactNumberObserver(
    viewModel: CommonCredentialsViewModel<*>
) {
    textChangeObserver { viewModel.contactNumber = it }
}

@BindingAdapter("errorMessageObserver")
fun TextInputLayout.errorMessageObserver(errorMessage: StateFlow<String>) {
    error = errorMessage.value
}

@BindingAdapter("showPasswordToggleObserver")
fun TextInputLayout.showPasswordToggleObserver(show: StateFlow<Boolean>) {
    isPasswordVisibilityToggleEnabled = show.value
}

@BindingAdapter("snackbarEvent")
fun View.snackbarEvent(message: String?) {
    if (!message.isNullOrEmpty()) {
        Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE).run {
            setAction(context.getString(R.string.dismiss)) {
                this.dismiss()
            }
            show()
        }
    }
}

@BindingAdapter("bindImage")
fun setImage(view: ImageView, image: Image?) {
    bindImageWith(view, image?.imageUri ?: image?.imageUrl)
}