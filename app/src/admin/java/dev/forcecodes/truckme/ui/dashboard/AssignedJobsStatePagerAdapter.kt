@file:Suppress("deprecation")

package dev.forcecodes.truckme.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder
import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder.IN_PROGRESS
import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder.PENDING
import dev.forcecodes.truckme.databinding.FragmentActiveJobsBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.startRealtimeMap
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val MAX_PAGER_SIZE = 2

class AssignedJobsStatePagerAdapter(
  fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

  override fun getItemCount(): Int = MAX_PAGER_SIZE

  override fun createFragment(position: Int): Fragment {
    return if (position == 0) {
      InProgressJobFragment()
    } else {
      PendingJobFragment()
    }
  }
}

@AndroidEntryPoint
abstract class ActiveJobsFragment(
  private val activeJobOder: ActiveJobOder
) : Fragment(R.layout.fragment_active_jobs) {

  protected val viewModel by viewModels<HomeDashboardViewModel>()
  private val binding by viewBinding(FragmentActiveJobsBinding::bind)

  private val deliveryAdapter = DeliveryAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.activeJobOrder(activeJobOder)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val message = if (activeJobOder == IN_PROGRESS) "ongoing" else "pending"
    binding.emptyMessage.text = getString(R.string.empty_message_state, message)

    binding.deliveryList.adapter = deliveryAdapter

    onDeliveryAdapterCreated(deliveryAdapter)

    deliveryAdapter.onActiveJobClick = { jobId ->
      startRealtimeMap(jobId)
    }

    repeatOnLifecycleParallel {
      launch {
        viewModel.emptyList.collect { isEmpty ->
          binding.emptyMessage.isVisible = isEmpty
        }

      }
      launch {
        delay(500L)
        binding.progressContainer.isGone = true
      }
    }
  }

  abstract fun onDeliveryAdapterCreated(adapter: DeliveryAdapter)
}

class InProgressJobFragment : ActiveJobsFragment(IN_PROGRESS) {

  override fun onDeliveryAdapterCreated(adapter: DeliveryAdapter) {

    observeOnLifecycleStarted {
      viewModel.activeJobsList.collect {
        adapter.submitList(it)
      }
    }
  }
}

class PendingJobFragment : ActiveJobsFragment(PENDING) {
  override fun onDeliveryAdapterCreated(adapter: DeliveryAdapter) {
    observeOnLifecycleStarted {
      viewModel.activeJobsList.collect {
        adapter.submitList(it)
      }
    }
  }
}