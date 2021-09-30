package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri

interface FleetItemListener {
  fun onDriverSelected(data: DriverUri)
  fun onVehicleSelected(data: VehicleUri)
}
