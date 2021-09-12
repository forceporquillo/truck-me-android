package dev.forcecodes.truckme.util

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.ui.auth.reset.PasswordResetViewModel
import dev.forcecodes.truckme.ui.auth.signin.FirebaseSignInViewModel
import kotlinx.coroutines.flow.StateFlow

@BindingAdapter("emailObserver")
fun AppCompatEditText.emailObserver(viewModelFirebase: FirebaseSignInViewModel) {
    textChangeObserver(viewModelFirebase::email)
}

@BindingAdapter("passwordObserver")
fun AppCompatEditText.passwordObserver(viewModelFirebase: FirebaseSignInViewModel) {
    textChangeObserver(viewModelFirebase::password)
}

@BindingAdapter("passwordResetObserver")
fun AppCompatEditText.passwordResetObserver(passwordResetViewModel: PasswordResetViewModel) {
    textChangeObserver(passwordResetViewModel::email)
}

@BindingAdapter("errorMessageObserver")
fun TextInputLayout.errorMessageObserver(errorMessage: StateFlow<String>) {
    error = errorMessage.value
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