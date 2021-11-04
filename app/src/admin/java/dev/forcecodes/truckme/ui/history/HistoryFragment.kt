package dev.forcecodes.truckme.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentHistoryBinding
import dev.forcecodes.truckme.databinding.ItemDeliveryBinding
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.history.HistoryAdapter.HistoryViewHolder

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

  private val binding by viewBinding(FragmentHistoryBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val historyAdapter = HistoryAdapter()
    binding.historyList.adapter = historyAdapter

    historyAdapter.submitList(
      listOf(
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        ),
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        ),
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        ),
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        ),
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        ),
        DeliveredItem(
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
          "Test",
        )
    ))
  }
}

data class DeliveredItem(
  val id: String,
  val title: String,
  val address: String,
  val items: String,
  val date: String,
  val time: String
)

private val DELIVERY_COMPARATOR = object : DiffUtil.ItemCallback<DeliveredItem>() {

  override fun areItemsTheSame(oldItem: DeliveredItem, newItem: DeliveredItem): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(oldItem: DeliveredItem, newItem: DeliveredItem): Boolean {
    return oldItem == newItem
  }
}

class HistoryAdapter : ListAdapter<DeliveredItem, HistoryViewHolder>(DELIVERY_COMPARATOR) {

  class HistoryViewHolder(
    private val binding: ItemDeliveryBinding
  ) : RecyclerView.ViewHolder(binding.root) {

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
    return HistoryViewHolder(
      ItemDeliveryBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

  }
}
