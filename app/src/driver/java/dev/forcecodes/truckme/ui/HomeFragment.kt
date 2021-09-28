package dev.forcecodes.truckme.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.ActiveJobItems
import dev.forcecodes.truckme.core.domain.AssignedJobsUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.databinding.AssignedJobItemBinding
import dev.forcecodes.truckme.databinding.FragmentHomeBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.startRealtimeMap
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.AssignedJobAdapter.AssignedViewHolder
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    assignedAdapter.onAssignedJobClick = {
      // todo add id
      startRealtimeMap()
    }
  }
}



