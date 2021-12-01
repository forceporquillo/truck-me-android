package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata

interface FleetItemListener {
  fun onDriverSelected(data: DriverUri)
  fun onVehicleSelected(data: VehicleUri)
  fun onDeleteFleet(id: String, type: FleetType)
  fun onFleetStateChanged(fleetMetadata: FleetStateUpdateMetadata)
}
