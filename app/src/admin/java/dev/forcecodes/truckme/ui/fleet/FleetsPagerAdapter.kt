package dev.forcecodes.truckme.ui.fleet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.ui.fleet.FleetPageType.DRIVER
import dev.forcecodes.truckme.ui.fleet.FleetPageType.VEHICLE
import kotlinx.coroutines.flow.collect

internal const val MAX_FLEET_SIZE = 2

class FleetsPagerAdapter(
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = MAX_FLEET_SIZE

  override fun createFragment(position: Int): Fragment {
    return if (position == 0) {
      DriverFleetFragment()
    } else {
      VehicleFleetFragment()
    }
  }
}