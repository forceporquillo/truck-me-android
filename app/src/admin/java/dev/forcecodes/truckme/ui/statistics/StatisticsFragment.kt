package dev.forcecodes.truckme.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.databinding.FragmentStatisticsBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
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

    viewModel.loadStatistics()

    observeOnLifecycleStarted {
      viewModel.dateList.collect {
        if (it.isNotEmpty()) {
          binding.datePicker.hint = it.first()
          viewModel.executeQuery(it.first())
          setUiState(it.distinct())
        }
      }
    }

    observeOnLifecycleStarted {
      viewModel.copyDeliveryInfo.collect { list ->
        childFragmentManager.fragments.forEach { fragment ->
          if (fragment is StatsPagerFragment) {
            fragment.onChangeSearch(list)
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

    binding.datePicker.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
      viewModel.executeQuery(newItem)
    }

    binding.daySpinner.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
      viewModel.type = newItem
      viewModel.reOrder()

      val hintType = if (newItem == getString(string.driver)) {
        getString(string.select_driver_type)
      } else {
        getString(string.select_date_type)
      }
      setUiState(viewModel.itemDelivered.value)
      binding.searchType.text = hintType
    }
  }

  private fun setUiState(items: List<String>) {
    binding.datePicker.apply {
      clearSelectedItem()
      if (items.isNotEmpty()) {
        hint = items[0]
      }
    }
    Timber.e("items $items")
    binding.datePicker.setItems(items)
    binding.datePicker.spinnerPopupHeight
  }
}