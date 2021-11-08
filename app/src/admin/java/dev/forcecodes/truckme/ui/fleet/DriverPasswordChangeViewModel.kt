package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.settings.UpdatePasswordV2UseCase
import dev.forcecodes.truckme.core.domain.settings.UpdatedPasswordV2
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverPasswordChangeViewModel @Inject constructor(
  private val updatePasswordV2UseCase: UpdatePasswordV2UseCase
) : ViewModel() {

  private val _enableSubmit = MutableStateFlow(false)
  val enableSubmit = _enableSubmit.asStateFlow()

  private val passwordFlow = MutableStateFlow("")
  private val newPasswordFlow = MutableStateFlow("")

  private val _isSuccess = Channel<Boolean>(Channel.RENDEZVOUS)
  val isSuccess = _isSuccess.receiveAsFlow()

  var newPassword: String = ""
    set(value) {
      passwordFlow.value = value
      field = value
    }

  var confirmNewPassword: String = ""
    set(value) {
      newPasswordFlow.value = value
      field = value
    }

  init {
    viewModelScope.launch {
      combine(
        passwordFlow,
        newPasswordFlow
      ) { password, newPassword ->
        password == newPassword &&
          (password.isNotEmpty() && newPassword.isNotEmpty())
      }.collect {
        _enableSubmit.value = it
      }
    }
  }

  fun submit(driverUri: DriverUri) {
    viewModelScope.launch {
      val updatePasswordV2 = UpdatedPasswordV2(
        driverUri.id,
        driverUri.email,
        driverUri.password,
        newPassword
      )

      val successOrFail = updatePasswordV2UseCase(updatePasswordV2).successOr(false)

      _isSuccess.tryOffer(successOrFail)
    }
  }
}