package dev.forcecodes.truckme.ui.statistics

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.forcecodes.truckme.ui.dashboard.MAX_PAGER_SIZE

class StatisticsStatePagerAdapter(
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = MAX_PAGER_SIZE

  override fun createFragment(position: Int): Fragment {
    return if (position == 0) {
      StatsReceivedItemsFragment()
    } else {
      StatsDeliveredItemsFragment()
    }
  }
}
