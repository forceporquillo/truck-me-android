package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.drawable
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.databinding.FragmentFleetBinding
import dev.forcecodes.truckme.extensions.dispatchWhenBackPress
import dev.forcecodes.truckme.extensions.getDrawable
import dev.forcecodes.truckme.extensions.navigate
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FleetFragment : Fragment(R.layout.fragment_fleet) {

  private val viewModel by viewModels<FleetViewModel>()
  private val binding by viewBinding(FragmentFleetBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    observeChanges()
    dispatchBackPress()
    initViewPager()
  }

  private fun initViewPager() = binding.apply {
    viewPager.adapter = FleetsPagerAdapter(this@FleetFragment)

    TabLayoutMediator(fleetTabLayout, viewPager) { tab, position ->
      when (position) {
        0 -> {
          tab.text = getString(string.driver)
          tab.icon = getDrawable(drawable.ic_person)
        }
        1 -> {
          tab.text = getString(string.vehicles)
          tab.icon = getDrawable(drawable.nav_ic_fleet)
        }
      }
    }.attach()
  }

  private fun observeChanges() = repeatOnLifecycleParallel {
    launch { viewModel.fleetNavActionEvent.collect(::collectNavUiEvent) }
  }

  private fun collectNavUiEvent(uiEvent: FleetNavActionEvent) {
    val destinationId = if (uiEvent is FleetNavActionEvent.AddVehicle) {
      R.id.to_addVehicleFragment
    } else {
      R.id.to_addDriverFragment
    }
    navigate(destinationId)
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