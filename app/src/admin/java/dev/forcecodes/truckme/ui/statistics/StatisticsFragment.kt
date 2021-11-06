package dev.forcecodes.truckme.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.domain.fleets.formattedDate
import dev.forcecodes.truckme.databinding.FragmentStatisticsBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

  private val binding by viewBinding(FragmentStatisticsBinding::bind)
  private val viewModel by viewModels<StatisticsViewModel>()

  private val listenerList = mutableListOf<StatisticsFragmentListener>()

  private fun isInheritStatsFragment(fragment: Fragment): Boolean {
    return fragment is StatsReceivedItemsFragment
      || fragment is StatsDeliveredItemsFragment
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.viewPager.adapter = StatisticsStatePagerAdapter(this)

    val formattedDate = formattedDate(System.currentTimeMillis())
    binding.datePicker.hint = formattedDate

    viewModel.searchBy(formattedDate)

    childFragmentManager.addFragmentOnAttachListener { _, fragment ->
      if (fragment is StatsPagerFragment) {
        if (isInheritStatsFragment(fragment)) {
          listenerList.add(fragment)
        }
        observeOnLifecycleStarted {
          viewModel.itemDelivered.collect { list ->
            listenerList.forEach {
              it.onChangeSearch(list)
            }
          }
        }
      }
    }

    repeatOnLifecycleParallel {
      launch {
        viewModel.dateList.collect(::setUiState)
      }
      launch {
        viewModel.driverList.collect(::setUiState)
      }
    }

    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      when (position) {
        0 -> tab.text = getString(string.received_items)
        1 -> tab.text = getString(string.delivered_items)
      }
    }.attach()

    try {
      binding.datePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
        viewModel.searchBy(newItem)
        viewModel.executeQuery(newItem)
      }

      binding.daySpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
        val hintType = if (newItem == getString(string.driver)) {
          getString(string.select_driver_type)
        } else {
          getString(string.select_date_type)
        }
        viewModel.type = newItem
        binding.searchType.text = hintType
      }
    } catch (e: IllegalStateException) {
      binding.daySpinner.dismiss()
      binding.datePicker.dismiss()
    }
  }

  private fun setUiState(items: List<String>) {
    with(binding.datePicker) {
      setItems(emptyList<String>())
      if (items.isNotEmpty()) {
        hint = items[0]
      }
      setItems(items)
    }
  }
}