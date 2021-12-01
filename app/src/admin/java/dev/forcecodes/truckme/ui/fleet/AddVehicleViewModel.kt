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
import timber.log.Timber.Forest
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

  private val _registraionNumberSf = MutableStateFlow("")
  val registraionNumberSf = _registraionNumberSf.asStateFlow()

  private val _maxLoadCapacity = MutableStateFlow("")
  val maxLoadCapacitySf = _maxLoadCapacity.asStateFlow()

  private val _enableSubmitButton = MutableStateFlow(false)
  val enableSubmitButton = _enableSubmitButton.asStateFlow()

  private val _showLoading = MutableStateFlow(false)
  val showLoading = _showLoading.asStateFlow()

  private val _uploadState = MutableStateFlow<FleetUploadState>(FleetUploadState.Loading)
  val uploadState = _uploadState.asStateFlow()

  private var isProfileSetExplicitly = false

  init {
    viewModelScope.launch {
      combine(
        vehicleNameSf,
        plateNumberSf,
        descriptionSf,
        registraionNumberSf,
        maxLoadCapacitySf
      ) { vehicle, plate, desc, registration, maxCapacity ->
        arrayOf(vehicle, plate, desc, registration, maxCapacity)
      }.map { fields ->
        isSameInstance(fields) ?: fields.all { it.isNotEmpty() }
      }.collect { enable ->
        _enableSubmitButton.value = enable
      }
    }
  }

  private fun isSameInstance(fields: Array<String>): Boolean? {
    if (fields.all { it.isEmpty() }) {
      return null
    }
    return vehicleUri?.run {
      !(fields[0] == name && fields[1] == plate
        && fields[2] == description && fields[3] == registrationNumber
        && fields[4] == maxLoadCapacity
        ) || isProfileSetExplicitly
    }
  }

  fun setProfileInBytes(profileIconInBytes: ByteArray) {
    this.profileIconInBytes = profileIconInBytes
    _enableSubmitButton.value = true
    isProfileSetExplicitly = true
  }

  var profileIconInBytes: ByteArray? = null

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

  var corNumber: String? = ""
    set(value) {
      value?.let {
        _registraionNumberSf.value = it
      }
      field = value
    }

  var maxLoadCapacity: String? = ""
    set(value) {
      value?.let {
        _maxLoadCapacity.value = it
      }
      field = value
    }

  var vehicleUri: VehicleUri? = null

  fun submit() {
    _enableSubmitButton.value = false

    val vehicleId = if (!vehicleUri?.id.isNullOrEmpty())
      vehicleUri?.id else UUID.randomUUID().toString()

    val vehicles = VehicleByteArray(
      id = vehicleId!!,
      name = vehicleName,
      plate = plateNumber,
      description = description,
      profile = profileIconInBytes,
      isActive = true,
      assignedAdminId = signInViewModelDelegate.userIdValue ?: "",
      registrationNumber = corNumber,
      loadCapacity = maxLoadCapacity
    )

    executeAppend(vehicles)
  }

  private fun executeAppend(vehicles: VehicleByteArray) {
    val useCase = addVehicleUseCase(vehicles)
    handleFleetAddition(useCase) { uploadState, isLoading ->
      submitAndSetLoading(isLoading, uploadState)
      _uploadState.value = uploadState
    }
  }

  private fun submitAndSetLoading(
    show: Boolean = true,
    uploadState: FleetUploadState
  ) {
    _showLoading.value = show

    _enableSubmitButton.value = when (uploadState) {
      is FleetUploadState.Success,
      is FleetUploadState.Loading -> false
      is FleetUploadState.Error -> true
    }
  }
}
