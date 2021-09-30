package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.data.fleets.VehicleByteArray
import dev.forcecodes.truckme.core.domain.fleets.AddVehicleUseCase
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
class AddVehicleViewModel @Inject constructor(
  private val addVehicleUseCase: AddVehicleUseCase,
  private val signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel() {

  private val _vehicleName = MutableStateFlow("")
  val vehicleNameSf = _vehicleName.asStateFlow()

  private val _plateNumber = MutableStateFlow("")
  val plateNumberSf = _plateNumber.asStateFlow()

  private val _description = MutableStateFlow("")
  val descriptionSf = _description.asStateFlow()

  private val _enableSubmitButton = MutableStateFlow(false)
  val enableSubmitButton = _enableSubmitButton.asStateFlow()

  private val _showLoading = MutableStateFlow(false)
  val showLoading = _showLoading.asStateFlow()

  private val _uploadState = MutableStateFlow<FleetUploadState>(FleetUploadState.Loading)
  val uploadState = _uploadState.asStateFlow()

  init {
    viewModelScope.launch {
      combine(vehicleNameSf, plateNumberSf, descriptionSf) { v, p, d ->
        arrayOf(v, p, d)
      }.map { fields ->
        fields.all { it.isNotEmpty() }
      }.collect { enable ->
        _enableSubmitButton.value = enable
      }
    }
  }

  var profileIconInBytes: ByteArray? = null
    set(value) {
      value?.let {
        //  enableSubmitButton(enable = true)
        //  sendUiEvent(BackPressDispatcherUiActionEvent.Intercept)
        //  isProfileAdded = true
        Timber.e(it.toString())
      }
      field = value
    }

  var vehicleName: String? = ""
    set(value) {
      value?.let {
        _vehicleName.value = it
      }
      field = value
    }

  var plateNumber: String? = ""
    set(value) {
      value?.let {
        _plateNumber.value = it
      }
      field = value
    }

  var description: String? = ""
    set(value) {
      value?.let {
        _description.value = it
      }
      field = value
    }

  var vehicleUri: VehicleUri? = null

  fun submit() {

    val vehicleId = if (!vehicleUri?.id.isNullOrEmpty())
      vehicleUri?.id else UUID.randomUUID().toString()

    val vehicles = VehicleByteArray(
      id = vehicleId!!,
      name = vehicleName,
      plate = plateNumber,
      description = description,
      profile = profileIconInBytes,
      isActive = false,
      assignedAdminId = signInViewModelDelegate.userIdValue ?: ""
    )

    executeAppend(vehicles)
  }

  private fun executeAppend(vehicles: VehicleByteArray) {
    val useCase = addVehicleUseCase(vehicles)
    handleFleetAddition(useCase) { uploadState, isLoading ->
      submitAndSetLoading(isLoading)
      _uploadState.value = uploadState
    }
  }

  private fun submitAndSetLoading(show: Boolean = true, enable: Boolean = show) {
    _showLoading.value = show

    // invert to disable the submit button
    _enableSubmitButton.value = enable
  }
}
