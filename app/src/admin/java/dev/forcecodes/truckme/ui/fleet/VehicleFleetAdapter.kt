package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri

class VehicleFleetAdapter(
  private val fleetItemListener: FleetItemListener,
  emptyState: (Boolean) -> Unit = {}
) : BaseFleetAdapter<VehicleUri>(emptyState, R.string.vehicles) {

  override fun onViewHolderCreated(data: VehicleUri) {
    fleetItemListener.onVehicleSelected(data)
  }
}