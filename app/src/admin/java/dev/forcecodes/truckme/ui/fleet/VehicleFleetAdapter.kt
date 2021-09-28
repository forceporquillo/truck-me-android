package dev.forcecodes.truckme.ui.fleet

import android.view.View
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri

class VehicleFleetAdapter(
  private val fleetItemListener: FleetItemListener? = null,
  emptyState: (Boolean) -> Unit
) : BaseFleetAdapter<VehicleUri>(emptyState, R.string.vehicles) {

  override fun onViewHolderCreated(root: View) {
    fleetItemListener?.onVehicleSelected()
  }
}