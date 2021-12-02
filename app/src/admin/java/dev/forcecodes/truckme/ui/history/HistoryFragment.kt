package dev.forcecodes.truckme.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentHistoryBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

  private val viewModel by viewModels<HistoryViewModel>()
  private val binding by viewBinding(FragmentHistoryBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val historyAdapter = HistoryAdapter()
    binding.historyList.adapter = historyAdapter

    observeOnLifecycleStarted {
      viewModel.historyList.collect(historyAdapter::submitList)
    }
  }
}
