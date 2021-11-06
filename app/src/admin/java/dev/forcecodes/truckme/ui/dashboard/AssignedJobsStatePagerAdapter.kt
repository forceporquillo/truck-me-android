@file:Suppress("deprecation")

package dev.forcecodes.truckme.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal const val MAX_PAGER_SIZE = 2

class AssignedJobsStatePagerAdapter(
  fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

  override fun getItemCount(): Int = MAX_PAGER_SIZE

  override fun createFragment(position: Int): Fragment {
    return if (position == 0) {
      InProgressJobFragment()
    } else {
      PendingJobFragment()
    }
  }
}