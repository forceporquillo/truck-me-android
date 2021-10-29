package dev.forcecodes.truckme.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.OnInterceptToolbarElevation
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentHomeBinding
import dev.forcecodes.truckme.extensions.getDrawable
import dev.forcecodes.truckme.extensions.navigateOnButtonClick
import dev.forcecodes.truckme.extensions.viewBinding

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

  private val binding by viewBinding(FragmentHomeBinding::bind)

  private lateinit var onInterceptToolbarElevation: OnInterceptToolbarElevation

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is MainActivity) {
      onInterceptToolbarElevation = context
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.apply {

      addButton.navigateOnButtonClick(R.id.to_map_nav_graph)

      viewPager.adapter = AssignedJobsStatePagerAdapter(requireActivity())

      TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        when (position) {
          0 -> {
            tab.text = getString(R.string.in_progress)
            tab.icon = getDrawable(R.drawable.ic_ongoing)
          }
          1 -> {
            tab.text = getString(R.string.pending)
            tab.icon = getDrawable(R.drawable.ic_in_progress)
          }
        }
      }.attach()
    }
  }

  override fun onStart() {
    super.onStart()
    onInterceptToolbarElevation.onRemoveToolbarElevation()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    onInterceptToolbarElevation.onAddToolbarElevation()
  }
}





