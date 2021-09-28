package dev.forcecodes.truckme.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.domain.dashboard.DeliveryItems
import dev.forcecodes.truckme.core.domain.dashboard.GetActiveJobsUseCase
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.databinding.FragmentHomeBinding
import dev.forcecodes.truckme.extensions.navigateOnButtonClick
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.startRealtimeMap
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
  activeJobsUseCase: GetActiveJobsUseCase
) : ViewModel() {

  private val _activeJobsList = MutableStateFlow<List<DeliveryItems>>(emptyList())
  val activeJobsList = _activeJobsList.asStateFlow()

  init {
    viewModelScope.launch {
      activeJobsUseCase.invoke(Any()).collect { result ->
        _activeJobsList.value = result.successOr(emptyList())
      }
    }
  }
}

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

  private val viewModel by viewModels<HomeDashboardViewModel>()
  private val binding by viewBinding(FragmentHomeBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val deliveryAdapter = DeliveryAdapter()
    binding.deliveryList.adapter = deliveryAdapter

    binding.addButton.navigateOnButtonClick(R.id.to_map_nav_graph)

    observeOnLifecycleStarted {
      viewModel.activeJobsList.collect(deliveryAdapter::submitList)
    }

    deliveryAdapter.onActiveJobClick = {
      // todo add id
      startRealtimeMap()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Timber.e(requestCode.toString())
  }
}





