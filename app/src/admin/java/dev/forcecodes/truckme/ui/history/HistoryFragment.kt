package dev.forcecodes.truckme.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dev.forcecodes.truckme.core.data.delivery.DeliveredItem
import dev.forcecodes.truckme.core.data.delivery.HistoryUseCase
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.databinding.FragmentHistoryBinding
import dev.forcecodes.truckme.databinding.ItemDeliveryBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import dev.forcecodes.truckme.ui.history.HistoryAdapter.HistoryViewHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

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
