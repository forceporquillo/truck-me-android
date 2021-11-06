package dev.forcecodes.truckme.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentStatsPagerBinding

abstract class StatsPagerFragment : Fragment(R.layout.fragment_stats_pager),
  StatisticsFragmentListener {

  protected val adapter by lazy { StatisticsItemAdapter() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val binding = FragmentStatsPagerBinding.bind(view)
    binding.statsListView.adapter = adapter
  }
}