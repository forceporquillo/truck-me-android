package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetType.DRIVER
import dev.forcecodes.truckme.core.data.fleets.FleetType.VEHICLE
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata
import timber.log.Timber
import timber.log.Timber.Forest

class VehicleFleetAdapter(
  private val fleetItemListener: FleetItemListener,
  emptyState: (Boolean) -> Unit = {}
) : BaseFleetAdapter<VehicleUri>(emptyState, R.string.vehicles) {

  override fun onViewHolderCreated(data: VehicleUri) {
    fleetItemListener.onVehicleSelected(data)
  }

  override fun onDeleteFleet(id: String) {
    fleetItemListener.onDeleteFleet(id, VEHICLE)
  }

  override fun onChangeActiveState(id: String, activeState: Boolean) {
    fleetItemListener.onFleetStateChanged(FleetStateUpdateMetadata(id, activeState, VEHICLE))
  }
}