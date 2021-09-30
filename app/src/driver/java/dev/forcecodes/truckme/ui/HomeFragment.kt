package dev.forcecodes.truckme.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentHomeBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.startRealtimeMap
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

  private val binding by viewBinding(FragmentHomeBinding::bind)

  private val viewModel by viewModels<HomeViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val assignedAdapter = AssignedJobAdapter()
    binding.assignJobList.adapter = assignedAdapter
    observeOnLifecycleStarted {

      viewModel.assignedJobsList.collect { value ->
        assignedAdapter.submitList(value.data)
        binding.progressIndicator.isVisible = value.isLoading
        binding.emptyActiveJob.isVisible = value.isEmpty
      }
    }

    assignedAdapter.onAssignedJobClick = { activeJobId ->
      startRealtimeMap(activeJobId)
    }
  }
}



