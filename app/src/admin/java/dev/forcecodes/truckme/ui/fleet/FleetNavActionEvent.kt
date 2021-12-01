package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri

sealed class FleetNavActionEvent : UiActionEvent {
  object AddDriver : FleetNavActionEvent()
  object AddVehicle : FleetNavActionEvent()
}

sealed class FleetUpdateNavAction {
  data class UpdateDriver(val data: DriverUri) : FleetUpdateNavAction()
  data class UpdateVehicle(val data: VehicleUri) : FleetUpdateNavAction()
}