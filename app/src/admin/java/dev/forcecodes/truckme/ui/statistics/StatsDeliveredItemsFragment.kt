package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.ItemDelivered

class StatsDeliveredItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<ItemDelivered>) {
    adapter.setItems(items.filter { it.deliveryInfo?.inbound == false }
      .map { it.deliveryInfo?.items ?: "" })
  }
}
