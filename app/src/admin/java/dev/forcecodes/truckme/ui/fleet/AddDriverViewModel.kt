package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.data.fleets.DriverByteArray
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.fleets.AddDriverUseCase
import dev.forcecodes.truckme.ui.auth.CommonCredentialsViewModel
import dev.forcecodes.truckme.ui.auth.handlePhoneNumber
import dev.forcecodes.truckme.ui.auth.isEmailValid
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

  private val _profileIcon = MutableStateFlow("")
  private val _fullName = MutableStateFlow("")
  private val _emailSf = MutableStateFlow("")
  private val _passwordSf = MutableStateFlow("")
  private val _confirmPassSf = MutableStateFlow("")
  private val _contactSf = MutableStateFlow("")
  private val _licenseNumber = MutableStateFlow("")
  private val _licenseExpiration = MutableStateFlow("")
  private val _restrictions = MutableStateFlow("")

  private val _uploadState = MutableStateFlow<FleetUploadState>(FleetUploadState.Loading)
  val uploadState = _uploadState.asStateFlow()

  private var phoneNumberInputStated = false
  private var isProfileSetExplicitly = false

  init {
    Timber.e(signInViewModelDelegate.userIdValue)
    checkRequireFields()
  }

  private fun checkRequireFields() {
    viewModelScope.launch {
      combineFleetInfo(
        _fullName,
        _emailSf,
        _passwordSf,
        _confirmPassSf,
        _contactSf,
        _licenseNumber,
        _licenseExpiration,
        _restrictions
      ) { fn, es, ps, cp, cf, ln, le, r ->
        arrayOf(fn, es, ps, cp, cf, ln, le, r)
      }.map { fields ->
        handlePasswordNotMatch(fields[2], fields[3])
        handlePhoneNumber(fields[4])
        isSameInstance(fields) ?: fields.all { string ->
          string.isNotEmpty() && (fields[2] == fields[3])
        }
      }.collect {
        enableSubmitButton(it)
      }
    }
  }

  private fun isSameInstance(fields: Array<String>): Boolean? {
    if (fields.all { it.isEmpty() }) {
      return null
    }
    return driverUri?.run {
      !(fields[0] == fullName && fields[1] == email
        && fields[2] == password && fields[3] == password
        && fields[4] == contact && fields[5] == licenseNumber
        && fields[6] == licenseExpiration && fields[7] == restrictions
        ) || isProfileSetExplicitly
    }
  }

  private val _enablePassword = MutableStateFlow(true)
  val enablePassword = _enablePassword.asStateFlow()

  var driverUri: DriverUri? = null
    set(value) {
      _enablePassword.value = true
      _passwordSf.value = value!!.password
      _confirmPassSf.value = value.password
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

  fun licenseNumber(value: String?) {
    _licenseNumber.value = value ?: ""
  }

  fun licenseExpiration(value: String?) {
    _licenseExpiration.value = value ?: ""
  }

  fun restrictions(value: String?) {
    _restrictions.value = value ?: ""
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

  var profileInBytes: ByteArray? = null

  fun profileIconInBytes(byteArray: ByteArray?) {
    _profileIcon.value = ""
    profileInBytes = byteArray
    Timber.e(profileInBytes.toString())

    if (driverUri != null && byteArray != null) {
      enableSubmitButton(true)
    }

    isProfileSetExplicitly = true
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
  ): Boolean {
    if (password.isNullOrEmpty()) {
      _passwordNotMatch.value = ""
      return false
    }

    if (confirmPassword.isNullOrEmpty()) {
      _passwordNotMatch.value = ""
      return false
    }

    _passwordNotMatch.value = if (password != confirmPassword) {
      INVALID_PASSWORD
    } else ""

    return password == confirmPassword
  }

  fun submit() {

    val driverId = when {
      !driverUri?.id.isNullOrEmpty() -> driverUri?.id
      else -> UUID.randomUUID().toString()
    }

    val driver = DriverByteArray(
      id = driverId!!,
      fullName = _fullName.value,
      email = _emailSf.value,
      password = _passwordSf.value,
      contact = _contactSf.value,
      isActive = driverUri?.isActive ?: true,
      profile = profileInBytes,
      assignedAdminId = signInViewModelDelegate.userIdValue!!,
      licenseNumber = _licenseNumber.value,
      licenseExpiration = _licenseExpiration.value,
      restrictions = _restrictions.value
    )

    handleFleetAddition(addDriverUseCase(driver)) { uploadState, isLoading ->
      val enableButtonState = when (uploadState) {
        is FleetUploadState.Success -> false
        is FleetUploadState.Loading -> false
        is FleetUploadState.Error -> true
      }

      submitAndSetLoading(isLoading, enableButtonState)
      _uploadState.value = uploadState
    }
  }
}