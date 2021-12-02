package dev.forcecodes.truckme.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.databinding.FragmentStatsPagerBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
abstract class StatsPagerFragment : Fragment(R.layout.fragment_stats_pager),
  StatisticsFragmentListener {

  protected val viewModel by viewModels<StatisticsViewModel>({ requireParentFragment() })
  protected val adapter by lazy { StatisticsItemAdapter() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val binding = FragmentStatsPagerBinding.bind(view)
    binding.statsListView.adapter = adapter

    observeOnLifecycleStarted {
      viewModel.itemDeliveredStats.collect {
        onStatsPage(it)
      }
    }
  }

  abstract fun onStatsPage(items: List<ItemDeliveredStats>)
}