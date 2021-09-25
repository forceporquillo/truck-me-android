package dev.forcecodes.truckme.ui.fleet

import android.view.View
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri

class DriverFleetAdapter(
  private val fleetItemListener: FleetItemListener? = null,
  emptyState: (Boolean) -> Unit = {}
) : BaseFleetAdapter<DriverUri>(emptyState, R.string.driver) {

  override fun onViewHolderCreated(root: View) {
    fleetItemListener?.onDriverSelected()
  }
}
