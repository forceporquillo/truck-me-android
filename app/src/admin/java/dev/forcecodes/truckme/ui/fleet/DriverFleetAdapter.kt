package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetType.DRIVER
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata

class DriverFleetAdapter(
  private val fleetItemListener: FleetItemListener,
  emptyState: (Boolean) -> Unit = {}
) : BaseFleetAdapter<DriverUri>(emptyState, R.string.driver) {

  override fun onViewHolderCreated(data: DriverUri) {
    fleetItemListener.onDriverSelected(data)
  }

  override fun onDeleteFleet(id: String) {
    fleetItemListener.onDeleteFleet(id, DRIVER)
  }

  override fun onChangeActiveState(id: String, activeState: Boolean) {
    fleetItemListener.onFleetStateChanged(FleetStateUpdateMetadata(id, activeState, DRIVER))
  }
}
