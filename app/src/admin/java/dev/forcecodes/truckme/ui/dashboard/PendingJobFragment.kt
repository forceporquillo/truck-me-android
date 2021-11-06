package dev.forcecodes.truckme.ui.dashboard

import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder.PENDING
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import kotlinx.coroutines.flow.collect

class PendingJobFragment : ActiveJobsFragment(PENDING) {
  override fun onDeliveryAdapterCreated(adapter: DeliveryAdapter) {
    observeOnLifecycleStarted {
      viewModel.activeJobsList.collect {
        adapter.submitList(it)
      }
    }
  }
}