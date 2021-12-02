package dev.forcecodes.truckme.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.domain.statistics.DAILY
import dev.forcecodes.truckme.core.domain.statistics.DRIVER
import dev.forcecodes.truckme.databinding.FragmentStatisticsBinding
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

  private val binding by viewBinding(FragmentStatisticsBinding::bind)
  private val viewModel by viewModels<StatisticsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.datePicker.lifecycleOwner = viewLifecycleOwner
    binding.daySpinner.lifecycleOwner = viewLifecycleOwner

    binding.viewPager.adapter = StatisticsStatePagerAdapter(this)

    repeatOnLifecycleParallel {
      launch {
        viewModel.filterType.collect {
          if (it.isNotEmpty()) {
            setUiState(it)
          }
        }
      }
    }

    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      when (position) {
        0 -> tab.text = getString(string.received_items)
        1 -> tab.text = getString(string.delivered_items)
      }
    }.attach()

    binding.datePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, itemSelected ->
      viewModel.filterStatsBy(itemSelected)
    }

    binding.daySpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, itemSelected ->
      val filter = when (itemSelected) {
        getString(string.driver) -> DRIVER
        else -> DAILY
      }

      viewModel.filterTypeBy(filter)

      val hintType = if (itemSelected == getString(string.driver)) {
        getString(string.select_driver_type)
      } else {
        getString(string.select_date_type)
      }

      binding.searchType.text = hintType
    }
  }

  private fun setUiState(items: List<String>) {
    binding.datePicker.apply {
      clearSelectedItem()
      if (items.isNotEmpty()) {
        viewModel.filterStatsBy(items[0])
        hint = items[0]
      }
    }

    binding.datePicker.setItems(items)
  }
}