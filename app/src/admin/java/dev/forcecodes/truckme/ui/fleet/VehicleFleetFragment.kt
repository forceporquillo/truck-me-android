package dev.forcecodes.truckme.ui.fleet

import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.ui.fleet.FleetPageType.VEHICLE
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class VehicleFleetFragment : BaseFleetFragment<VehicleUri, VehicleFleetAdapter>(VEHICLE) {

  override val fleetAdapter: VehicleFleetAdapter
    get() = VehicleFleetAdapter(this)

  override suspend fun observeFleetChanges(fleetAdapter: VehicleFleetAdapter) {
    viewModel.vehicleList.collect { fleetAdapter.submitList(it) }
  }
}