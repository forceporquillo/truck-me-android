package dev.forcecodes.truckme.ui.auth

import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.forcecodes.truckme.base.BaseViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.data
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class CommonCredentialsViewModel<E : UiActionEvent>(
    requireConfirmation: Boolean = false,
    var requireContactNumber: Boolean = false
) : BaseAuthViewModel<E>(requireConfirmation) {

    private val _invalidContactNumber = MutableStateFlow("")
    val invalidContactNumber = _invalidContactNumber.asStateFlow()

    protected var fromRemoteSource: Boolean = false

    open var contactNumber: String? = null
        set(value) {
            if (requireContactNumber && fromRemoteSource) {
                _invalidContactNumber.value = value?.run {
                    return@run INVALID_NUMBER takeIfPHDialCode this ?: ""
                } ?: ""
                if (value?.isEmpty() == true) {
                    _invalidContactNumber.value = CONTACT_CANNOT_BE_EMPTY
                } else {
                    enableSubmitButton(value?.length == 11)
                }
            }
            field = value
        }

    private infix fun String.takeIfPHDialCode(string: String) = this.takeIf {
        return@takeIf if (string.length > 1) {
            !string.startsWith("0")
        } else {
            !string.startsWith("09")
        }
    }
}

abstract class BaseAuthViewModel<E : UiActionEvent>(
    private val requireConfirmation: Boolean = false
) : BaseViewModel<E>() {

    // region
    private val _snackbarMessage = MutableStateFlow<String?>("")
    val snackbarMessage = _snackbarMessage.asStateFlow()

    private val _invalidEmail = MutableStateFlow<String?>("")
    val invalidEmail = _invalidEmail.asStateFlow()

    private val _passwordError = MutableStateFlow("")
    val passwordError = _passwordError.asStateFlow()

    private val _enableSubmitButton = MutableStateFlow(false)
    val enableSubmitButton = _enableSubmitButton.asStateFlow()

    private val _showLoading = MutableStateFlow(false)
    val showLoading = _showLoading.asStateFlow()

    private val _passwordShowToggle = MutableStateFlow(false)
    val passwordShowToggle = _passwordShowToggle.asStateFlow()


    private val _confirmPasswordShowToggle = MutableStateFlow(false)
    val confirmPasswordShowToggle = _confirmPasswordShowToggle.asStateFlow()

    private val _passwordNotMatch = MutableStateFlow("")
    val passwordNotMatch = _passwordNotMatch.asStateFlow()
    // endregion

    protected var isProfileAdded: Boolean = false

    // region Common
    open var email: String? = null
        set(value) {
            _invalidEmail.value = ""
            value?.isEmailValid { isValid ->
                _enableSubmitButton.value = isValid
                field = value
            }
        }

    var password: String? = null
        set(value) {
            _passwordError.value = ""
            _passwordShowToggle.value = !value.isNullOrEmpty()
            enableWhenPasswordMatch(value, password, !value.isNullOrEmpty() && value.length >= 6)
            value.checkPasswordEmpty(confirmPassword)
            field = value
        }
    // endregion

    open var confirmPassword: String? = null
        set(value) {
            enableWhenPasswordMatch(value, password, !value.isNullOrEmpty() && value.length >= 6)
            value?.let {
                password?.let { pw ->
                    _passwordNotMatch.value =
                        if (it != pw) {
                            INVALID_PASSWORD
                        } else ""
                }
            }
            value.checkPasswordEmpty(password)
            field = value
        }

    internal inline fun <T> fetchResult(
        result: Result<T>,
        crossinline block: (T?, e: Exception?) -> Unit
    ) {
        when (result) {
            is Result.Success<T> -> {
                block(result.data, null)
                submitAndSetLoading(false)
            }
            is Result.Error -> {
                Timber.e(result.exception)
                handleError(result.exception)
                block(null, result.exception)
                submitAndSetLoading(show = false, enable = true)
            }
            is Result.Loading -> { }
        }
    }

    private fun String?.checkPasswordEmpty(value: String?) {
        if (this?.isEmpty() == true && value !== null) {
            _passwordNotMatch.value = ""
        }
    }

    fun submitAndSetLoading(show: Boolean = true, enable: Boolean = show) {
        _showLoading.value = show

        // invert to disable the submit button
        _enableSubmitButton.value = enable
    }

    fun enableSubmitButton(enable: Boolean) {
        _enableSubmitButton.value = enable
    }

    private fun handleError(exception: Exception) {
        submitAndSetLoading(false)
        when (exception) {
            is FirebaseNetworkException -> {
                sendSnackbarMessage(exception.message)
            }
            is FirebaseAuthInvalidCredentialsException -> {
                // for invalid password credentials
                _passwordError.value = exception.message.toString()
            }
            else -> {
                _invalidEmail.value = exception.message
                Timber.e(exception.toString())
            }
        }
    }

    private fun enableWhenPasswordMatch(
        value1: String?,
        value2: String?,
        args: Boolean? = null
    ) {
        if (!requireConfirmation) {
            return
        }
        if (isProfileAdded) {
            return
        }

        enableSubmitButton(!value1.isNullOrEmpty() &&
                !value2.isNullOrEmpty() && value1 == value2 && args ?: true)
    }

    fun sendSnackbarMessage(message: String? = PROFILE_UPDATED) {
        _snackbarMessage.value = message
        viewModelScope.launch {
            // clear snackbar
            delay(250L)
            _snackbarMessage.value = ""
        }
    }

    companion object {
        const val PROFILE_UPDATED = "Profile successfully updated."
        const val INVALID_NUMBER = "Invalid contact number."
        const val INVALID_PASSWORD = "Password doesn't match."
        const val CONTACT_CANNOT_BE_EMPTY = "Contact number cannot be empty."
    }
}

// add some accepted providers here
val emailProviders = listOf("Gmail", "Yahoo")

// check if an email contains at least the accepted email providers provided
inline fun String.isEmailValid(
    crossinline contains: (Boolean) -> Unit
) {
    for (provider in emailProviders) {
        if (this.endsWith("@${provider.lowercase()}.com")) {
            contains(true)
            break
        }
        contains(false)
    }
}
