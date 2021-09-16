package dev.forcecodes.truckme.ui.account

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.domain.settings.UploadProfileUseCase
import dev.forcecodes.truckme.core.domain.settings.*
import dev.forcecodes.truckme.core.domain.settings.GetPhoneNumberUseCase
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.ui.auth.CommonCredentialsViewModel
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val viewModelDelegate: SignInViewModelDelegate,
    private val uploadProfileUseCase: UploadProfileUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val updatePhoneNumberUseCase: UpdatePhoneNumberUseCase,
    // real time updates
    getPhoneNumberUseCase: GetPhoneNumberUseCase
) : CommonCredentialsViewModel<UiActionEvent>(
    requireConfirmation = true,
    requireContactNumber = false
), SignInViewModelDelegate by viewModelDelegate {

    override var email: String? = userInfo.value?.getEmail()

    private var _contactNumberCopy: String? = ""

    init {
        viewModelScope.launch {
            userIdValue?.let { userId ->
                getPhoneNumberUseCase(userId).collect {
                    fromRemoteSource = true

                    if (it is Result.Success) {
                        _contactNumberStateFlow.value = it.data?.phoneNumber
                        _contactNumberCopy = it.data?.phoneNumber

                        delay(1500L)
                        // enable update and error handling once it emits values
                        if (!requireContactNumber) {
                            requireContactNumber = true
                        }
                    }
                }
            }
        }
    }

    override var contactNumber: String?
        get() = super.contactNumber
        set(value) {
            if (value == _contactNumberCopy) {
                // no need to update. so, we disable the submit button.
                // see base class [CommonCredentialsViewModel]
                return
            }
            super.contactNumber = value
        }

    var profileIcon: ByteArray? = null
        set(value) {
            value?.let {
                enableSubmitButton(enable = true)
                sendUiEvent(BackPressDispatcherUiActionEvent.Intercept)
                isProfileAdded = true
            }
            field = value
        }

    private val _oldPasswordInvalid = MutableStateFlow(OldPasswordWithErrorMessage())
    val oldPasswordInvalid = _oldPasswordInvalid.asStateFlow()

    private val _passwordConfirmActions =
        Channel<BottomSheetNavActions>(capacity = Channel.CONFLATED)
    val passwordConfirmActions = _passwordConfirmActions.receiveAsFlow()

    private val _oldPasswordShowToggle = MutableStateFlow(false)
    val oldPasswordShowToggle = _oldPasswordShowToggle.asStateFlow()

    private val _enableOldPasswordButton = MutableStateFlow(false)
    val enableOldPasswordButton = _enableOldPasswordButton.asStateFlow()

    var oldPassword: String = ""
        set(value) {
            _oldPasswordShowToggle.value = value.isNotEmpty()
            _enableOldPasswordButton.value = value.isNotEmpty()
            field = value
        }

    private val _contactNumberStateFlow = MutableStateFlow<String?>("")
    val contactNumberStateFlow = _contactNumberStateFlow.asStateFlow()

    val backPressUiEvent = super.mUiEvents.receiveAsFlow()

    fun saveChangesClick() {
        sendUiEvent(BackPressDispatcherUiActionEvent.Nothing)
        viewModelScope.launch {
            uploadProfile()
            updatePassword()
            updatePhoneNumber()
        }
    }

    private suspend fun updatePhoneNumber() {
        contactNumber?.let { number ->
            submitAndSetLoading()
            val result = updatePhoneNumberUseCase(Pair(userIdValue!!, PhoneNumber(number)))
            fetchResult(result) { phoneNumberData, _ ->
                phoneNumberData?.let {
                    if (it.isSuccess) {
                        sendSnackbarMessage()
                        enableSubmitButton(false)
                    }
                }
            }
        }
    }

    private suspend fun uploadProfile() {
        profileIcon?.let { profileInBytes ->
            submitAndSetLoading()

            val profileData = ProfileData(userIdValue, profileInBytes)
            val result = uploadProfileUseCase.invoke(profileData)

            fetchResult(result) { uploadResult, _ ->
                uploadResult?.let {
                    if (it.isSuccess) {
                        sendSnackbarMessage()
                        enableSubmitButton(false)
                    }
                }
            }
        }
    }

    fun changePasswordClick() {
        saveChangesClick()
    }

    private suspend fun updatePassword() {
        password?.let { newPassword ->
            submitAndSetLoading()
            Timber.e("updatePassword $newPassword")
            Timber.e("oldPassword $oldPassword")

            if (oldPassword.isEmpty()) {
                submitAndSetLoading(false)
                passwordNavActions(BottomSheetNavActions.PasswordConfirmActions())
                return@let
            }

            val currentUserPassword = CurrentUserPassword(email!!, newPassword, oldPassword)
            val result = updatePasswordUseCase(currentUserPassword)

            fetchResult(result) { passwordUpdate: PasswordUpdate?, e: Exception? ->
                passwordUpdate?.let {
                    if (it.isSuccess) {
                        passwordNavActions(BottomSheetNavActions.DismissPasswordActions)
                    }
                }
                e?.let {
                    _oldPasswordInvalid.value = OldPasswordWithErrorMessage(oldPassword, it.message)
                    val navActions = BottomSheetNavActions.PasswordConfirmActions(it.message)
                    passwordNavActions(navActions)
                }
            }
        }

    }

    private fun passwordNavActions(actions: BottomSheetNavActions) {
        _passwordConfirmActions.trySend(actions)
    }
}

sealed class BackPressDispatcherUiActionEvent : UiActionEvent {
    object Intercept : BackPressDispatcherUiActionEvent()
    object Nothing : BackPressDispatcherUiActionEvent()
}

sealed class BottomSheetNavActions {
    data class PasswordConfirmActions(val errorMessage: String? = "") : BottomSheetNavActions()
    object DismissPasswordActions : BottomSheetNavActions()
}

data class OldPasswordWithErrorMessage(
    val oldPassword: String? = "",
    val errorMessage: String? = ""
)