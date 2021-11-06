package dev.forcecodes.truckme.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.core.data.delivery.DeliveredItem
import dev.forcecodes.truckme.databinding.ItemDeliveryBinding
import dev.forcecodes.truckme.ui.history.HistoryAdapter.HistoryViewHolder

class HistoryAdapter : ListAdapter<DeliveredItem, HistoryViewHolder>(
  object : DiffUtil.ItemCallback<DeliveredItem>() {

    override fun areItemsTheSame(oldItem: DeliveredItem, newItem: DeliveredItem): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DeliveredItem, newItem: DeliveredItem): Boolean {
      return oldItem == newItem
    }
  }) {

  class HistoryViewHolder(
    private val binding: ItemDeliveryBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DeliveredItem) {
      binding.apply {
        title.text = item.title
        time.text = item.time
        address.text = item.address
        date.text = item.date
        productType.text = item.items
      }
    }
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
    holder.bind(getItem(position))
  }
}