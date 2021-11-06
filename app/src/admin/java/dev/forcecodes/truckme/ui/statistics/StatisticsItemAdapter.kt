package dev.forcecodes.truckme.ui.statistics

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.databinding.ItemsStatisticsBinding
import dev.forcecodes.truckme.ui.statistics.StatisticsItemAdapter.StatisticsItemViewHolder

class StatisticsItemAdapter : RecyclerView.Adapter<StatisticsItemViewHolder>() {

  private var items = listOf<String>()

  @SuppressLint("NotifyDataSetChanged")
  fun setItems(items: List<String>) {
    this.items = items
    notifyDataSetChanged()
  }

  class StatisticsItemViewHolder(
    private val binding: ItemsStatisticsBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun setItems(item: String) {
      binding.itemTitle.text = item
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