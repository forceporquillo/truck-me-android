package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata
import dev.forcecodes.truckme.databinding.FragmentFleetPagesBinding
import dev.forcecodes.truckme.extensions.navigate
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.viewBinding

abstract class BaseFleetFragment<F : FleetUiModel, T : BaseFleetAdapter<out F>>(
  private val type: FleetPageType
) : Fragment(R.layout.fragment_fleet_pages), FleetItemListener {

  private val binding by viewBinding(FragmentFleetPagesBinding::bind)
  protected val viewModel by viewModels<FleetViewModel>({ requireParentFragment() })

  abstract val fleetAdapter: T

  abstract suspend fun observeFleetChanges(fleetAdapter: T)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = fleetAdapter

    binding.fleetList.adapter = adapter
    observeOnLifecycleStarted { observeFleetChanges(adapter) }
  }

  override fun onDriverSelected(data: DriverUri) {
    viewModel.onDriverSelected(data)
  }

  override fun onVehicleSelected(data: VehicleUri) {
    viewModel.onVehicleSelected(data)
  }

  override fun onDeleteFleet(id: String, type: FleetType) {
    viewModel.onDeleteFleet(id, type)
  }

  override fun onFleetStateChanged(fleetMetadata: FleetStateUpdateMetadata) {
    viewModel.updateFleetState(fleetMetadata)
  }
}