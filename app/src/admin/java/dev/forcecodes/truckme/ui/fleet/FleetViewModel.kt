package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.BaseViewModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveVehicleFleetsUseCase
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FleetViewModel @Inject constructor(
  observeVehicleFleetsUseCase: ObserveVehicleFleetsUseCase,
  observeDriverFleetsUseCase: ObserveDriverFleetsUseCase,
  private val signInViewModelDelegate: SignInViewModelDelegate
) : BaseViewModel<FleetNavActionEvent>() {

  private val _vehicleList = MutableStateFlow<List<FleetUiModel.VehicleUri>>(emptyList())
  val vehicleList = _vehicleList.asStateFlow()

  private val _driverList = MutableStateFlow<List<FleetUiModel.DriverUri>>(emptyList())
  val driverList = _driverList

  private val _fleetLoadStatueIsLoading = MutableStateFlow(true)
  val fleetLoadStatueIsLoading = _fleetLoadStatueIsLoading.asStateFlow()

  init {
    viewModelScope.launch {
      launch {
        observeVehicleFleetsUseCase(signInViewModelDelegate.userIdValue!!).collect {
          _vehicleList.value = it.successOr(emptyList())
          _fleetLoadStatueIsLoading.value = false
        }
      }
      launch {
        observeDriverFleetsUseCase(signInViewModelDelegate.userIdValue!!).collect {
          _driverList.value = it.successOr(emptyList())
          _fleetLoadStatueIsLoading.value = false
        }
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

  companion object {
    const val DELAY_MILLIS = 300L
  }
}