package dev.forcecodes.truckme.ui.fleet

import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.ui.fleet.FleetPageType.DRIVER
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class DriverFleetFragment : BaseFleetFragment<DriverUri, DriverFleetAdapter>(DRIVER) {

  override val fleetAdapter: DriverFleetAdapter
    get() = DriverFleetAdapter(this)

  override suspend fun observeFleetChanges(fleetAdapter: DriverFleetAdapter) {
    viewModel.driverList.collect { fleetAdapter.submitList(it) }
  }
}