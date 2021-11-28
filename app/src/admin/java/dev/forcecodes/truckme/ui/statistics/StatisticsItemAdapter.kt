package dev.forcecodes.truckme.ui.statistics

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.databinding.ItemsStatisticsBinding
import dev.forcecodes.truckme.ui.statistics.StatisticsItemAdapter.StatisticsItemViewHolder

class StatisticsItemAdapter : RecyclerView.Adapter<StatisticsItemViewHolder>() {

  private var items = listOf<ItemDeliveredStats>()

  @SuppressLint("NotifyDataSetChanged")
  fun setItems(items: List<ItemDeliveredStats>) {
    this.items = items
    notifyDataSetChanged()
  }

  class StatisticsItemViewHolder(
    private val binding: ItemsStatisticsBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun setItems(item: ItemDeliveredStats) {
      with(binding) {
        title.text = item.itemTitle
        documentId.text = item.documentId
        timeStarted.text = item.timeStarted
        etaTimeText.text = item.estimatedTimeArrival
        timeCompletedText.text = item.timeCompleted
        dateText.text = item.dateAccomplish
        deliveryState.text = item.deliveryStatus
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsItemViewHolder {
    return StatisticsItemViewHolder(
      ItemsStatisticsBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: StatisticsItemViewHolder, position: Int) {
    holder.setItems(items[position])
  }

  override fun getItemCount(): Int {
    return items.size
  }
}