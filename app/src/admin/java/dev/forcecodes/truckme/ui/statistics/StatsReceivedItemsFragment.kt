package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats

class StatsReceivedItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<ItemDeliveredStats>) {
    // space for rent
  }

  override fun onStatsPage(items: List<ItemDeliveredStats>) {
    adapter.setItems(items.filter { it.metadata.bound })
  }
}