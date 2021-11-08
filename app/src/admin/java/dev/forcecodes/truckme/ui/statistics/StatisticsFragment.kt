package dev.forcecodes.truckme.ui.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView
import com.skydoves.powerspinner.databinding.ItemDefaultPowerSpinnerLibraryBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.data.delivery.formatToDate
import dev.forcecodes.truckme.databinding.FragmentStatisticsBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.toast
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

    binding.datePicker.lifecycleOwner = viewLifecycleOwner
    binding.daySpinner.lifecycleOwner = viewLifecycleOwner

    binding.viewPager.adapter = StatisticsStatePagerAdapter(this)

    val formattedDate = formatToDate()

    binding.datePicker.hint = formattedDate

    childFragmentManager.addFragmentOnAttachListener { _, fragment ->
      if (fragment is StatsPagerFragment) {
        if (isInheritStatsFragment(fragment)) {
          listenerList.add(fragment)
        }
        observeOnLifecycleStarted {
          viewModel.copyDeliveryInfo.collect { list ->
            listenerList.forEach { listener ->
              listener.onChangeSearch(list)
            }
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