package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.BaseViewModel
import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.domain.fleets.DeleteFleetUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveVehicleFleetsUseCase
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata
import dev.forcecodes.truckme.core.domain.fleets.UpdateFleetStateUseCase
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.core.util.tryOffer
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import dev.forcecodes.truckme.ui.fleet.FleetUpdateNavAction.UpdateDriver
import dev.forcecodes.truckme.ui.fleet.FleetUpdateNavAction.UpdateVehicle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FleetViewModel @Inject constructor(
  private val deleteFleetUseCase: DeleteFleetUseCase,
  private val updateFleetStateUseCase: UpdateFleetStateUseCase,
  observeVehicleFleetsUseCase: ObserveVehicleFleetsUseCase,
  observeDriverFleetsUseCase: ObserveDriverFleetsUseCase,
  signInViewModelDelegate: SignInViewModelDelegate
) : BaseViewModel<FleetNavActionEvent>() {

  private val _vehicleList = MutableStateFlow<List<FleetUiModel.VehicleUri>>(emptyList())
  val vehicleList = _vehicleList.asStateFlow()

  private val _driverList = MutableStateFlow<List<FleetUiModel.DriverUri>>(emptyList())
  val driverList = _driverList

  private val _fleetLoadStatueIsLoading = MutableStateFlow(true)
  val fleetLoadStatueIsLoading = _fleetLoadStatueIsLoading.asStateFlow()

  private val _onUpdateActionEvent = Channel<FleetUpdateNavAction>(capacity = Channel.CONFLATED)
  val onUpdateActionEvent = _onUpdateActionEvent.receiveAsFlow()

  init {
    val adminId = signInViewModelDelegate.userIdValue!!
    viewModelScope.launch {
      launch {
        observeVehicleFleetsUseCase(adminId).collect {
          _vehicleList.value = it.successOr(emptyList())
          _fleetLoadStatueIsLoading.value = false
        }
      }
      launch {
        observeDriverFleetsUseCase(adminId).collect {
          _driverList.value = it.successOr(emptyList())
          _fleetLoadStatueIsLoading.value = false
        }
      }
    }
  }

  fun updateFleetState(fleetStateUpdateMetadata: FleetStateUpdateMetadata) {
    viewModelScope.launch {
      updateFleetStateUseCase(fleetStateUpdateMetadata)
    }
  }

  fun onDeleteFleet(id: String, type: FleetType) {
    viewModelScope.launch {
      deleteFleetUseCase.invoke(Pair(id, type)).collect {
        Timber.e(it.toString())
      }
    }
  }

  val fleetNavActionEvent = mUiEvents.receiveAsFlow()
    .debounce(DELAY_MILLIS)

  fun addDriverClick() {
    sendUiEvent(FleetNavActionEvent.AddDriver)
  }

  fun addVehicleClick() {
    sendUiEvent(FleetNavActionEvent.AddVehicle)
  }

  fun onDriverSelected(data: DriverUri) {
    _onUpdateActionEvent.tryOffer(UpdateDriver(data))
  }

  fun onVehicleSelected(data: VehicleUri) {
    _onUpdateActionEvent.tryOffer(UpdateVehicle(data))
  }

  companion object {
    const val DELAY_MILLIS = 300L
  }
}
