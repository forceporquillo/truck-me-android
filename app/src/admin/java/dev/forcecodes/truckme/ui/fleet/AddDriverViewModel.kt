package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.data.fleets.DriverByteArray
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.domain.fleets.AddDriverUseCase
import dev.forcecodes.truckme.ui.auth.CommonCredentialsViewModel
import dev.forcecodes.truckme.ui.auth.handlePhoneNumber
import dev.forcecodes.truckme.ui.auth.isEmailValid
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddDriverViewModel @Inject constructor(
  private val addDriverUseCase: AddDriverUseCase,
  private val signInViewModelDelegate: SignInViewModelDelegate
) : CommonCredentialsViewModel<UiActionEvent>(requireConfirmation = true),
  SignInViewModelDelegate by signInViewModelDelegate {

  private val _fullName = MutableStateFlow("")
  private val _emailSf = MutableStateFlow("")
  private val _passwordSf = MutableStateFlow("")
  private val _confirmPassSf = MutableStateFlow("")
  private val _contactSf = MutableStateFlow("")

  private val _uploadState = MutableStateFlow<FleetUploadState>(FleetUploadState.Loading)
  val uploadState = _uploadState.asStateFlow()

  private var phoneNumberInputStated = false

  init {
    Timber.e(signInViewModelDelegate.userIdValue)
    viewModelScope.launch {
      combine(
        _fullName,
        _emailSf,
        _passwordSf,
        _confirmPassSf,
        _contactSf
      ) { f, e, p, cp, c ->
        arrayOf(f, e, p, cp, c)
      }.map { fields ->
        handlePhoneNumber(fields.last())
        handlePasswordNotMatch(fields[2], fields[3])

        fields.all {
          it.isNotEmpty()
            && (fields[2] == fields[3])
        }
      }.collect(::enableSubmitButton)
    }
  }

  private val _enablePassword = MutableStateFlow(true)
  val enablePassword = _enablePassword.asStateFlow()

  var driverUri: DriverUri? = null
    set(value) {
      _enablePassword.value = true
      field = value
    }

  fun fullName(value: String?) {
    _fullName.value = value ?: ""
  }

  fun email(value: String?) {
    _emailSf.value = value ?: ""

    if (_emailSf.value.isEmpty()) {
      return
    }

    viewModelScope.launch {
      _emailSf.isEmailValid {
        Timber.e("is valid $it")
        setInvalidEmail(it)
      }
    }
  }

  fun password(value: String?) {
    _passwordSf.value = value ?: ""
  }

  fun confirmPassword(value: String?) {
    _confirmPassSf.value = value ?: ""
  }

  fun contactNumber(value: String?) {
    phoneNumberInputStated = true
    _contactSf.value = value ?: ""
  }

  private var profileInBytes: ByteArray? = null

  fun profileIconInBytes(byteArray: ByteArray?) {
    profileInBytes = byteArray
  }

  private fun handlePhoneNumber(field: String?) {
    if (!phoneNumberInputStated) {
      return
    }

    _invalidContactNumber.handlePhoneNumber(field, "") {}
  }

  private fun handlePasswordNotMatch(
    password: String?,
    confirmPassword: String?
  ) {
    if (password.isNullOrEmpty()) {
      _passwordNotMatch.value = ""
      return
    }

    if (confirmPassword.isNullOrEmpty()) {
      _passwordNotMatch.value = ""
      return
    }

    _passwordNotMatch.value = if (password != confirmPassword) {
      INVALID_PASSWORD
    } else ""
  }

  fun submit() {
    submitAndSetLoading(true)
    val driverId = if (!driverUri?.id.isNullOrEmpty()) driverUri?.id else UUID.randomUUID().toString()

    val driver = DriverByteArray(
      id = driverId!!,
      fullName = _fullName.value,
      email = _emailSf.value,
      password = _passwordSf.value,
      contact = _contactSf.value,
      isActive = false,
      profile = profileInBytes,
      assignedAdminId = signInViewModelDelegate.userIdValue!!
    )

    handleFleetAddition(addDriverUseCase(driver)) { uploadState, isLoading ->
      submitAndSetLoading(isLoading)
      _uploadState.value = uploadState
    }
  }
}