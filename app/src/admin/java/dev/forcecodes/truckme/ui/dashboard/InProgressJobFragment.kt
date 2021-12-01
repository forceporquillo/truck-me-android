package dev.forcecodes.truckme.ui.dashboard

import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder.IN_PROGRESS
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import kotlinx.coroutines.flow.collect

class InProgressJobFragment : ActiveJobsFragment(IN_PROGRESS) {

  override fun onDeliveryAdapterCreated(adapter: DeliveryAdapter) {

    observeOnLifecycleStarted {
      viewModel.activeJobsList.collect {
        adapter.submitList(it)
      }
    }
  }
}
