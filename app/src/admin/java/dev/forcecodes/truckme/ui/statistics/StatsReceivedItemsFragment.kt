package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.ItemDelivered

class StatsReceivedItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<ItemDelivered>) {
    adapter.setItems(items.filter { it.deliveryInfo?.inbound == true }
      .map { it.deliveryInfo?.items ?: "" })
  }
}