package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri

class DriverFleetAdapter(
  private val fleetItemListener: FleetItemListener,
  emptyState: (Boolean) -> Unit = {}
) : BaseFleetAdapter<DriverUri>(emptyState, R.string.driver) {

  override fun onViewHolderCreated(data: DriverUri) {
    fleetItemListener.onDriverSelected(data)
  }
}
