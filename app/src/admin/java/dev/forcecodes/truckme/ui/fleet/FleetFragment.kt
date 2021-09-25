package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentFleetBinding
import dev.forcecodes.truckme.extensions.dispatchWhenBackPress
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FleetFragment : Fragment(R.layout.fragment_fleet) {

  private val viewModel by viewModels<FleetViewModel>()
  private val binding by viewBinding(FragmentFleetBinding::bind)

  private lateinit var vehicleAdapter: VehicleFleetAdapter
  private lateinit var driverAdapter: DriverFleetAdapter

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    vehicleAdapter = VehicleFleetAdapter {}
    driverAdapter = DriverFleetAdapter {}

    observeChanges()
    dispatchBackPress()
    initAdapter()
  }

  private fun initAdapter() {
    val concatAdapter = ConcatAdapter(driverAdapter, vehicleAdapter)
    binding.fleetList.adapter = concatAdapter
  }

  private fun observeChanges() = repeatOnLifecycleParallel {
    launch {
      viewModel.vehicleList.collect(vehicleAdapter::submitList)
    }
    launch {
      viewModel.driverList.collect(driverAdapter::submitList)
    }
    launch {
      viewModel.fleetNavActionEvent.collect { uiEvent ->
        val destinationId = if (uiEvent is FleetNavActionEvent.AddVehicle) {
          R.id.to_addVehicleFragment
        } else {
          R.id.to_addDriverFragment
        }
        findNavController().navigate(destinationId)
      }
    }
  }

  private fun dispatchBackPress() {
    dispatchWhenBackPress {
      if (binding.expandableLayout.isOpen()) {
        binding.expandableLayout.close()
      } else {
        navigateUp()
      }
    }
  }
}