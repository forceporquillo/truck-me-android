package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.ItemDelivered

class StatsReceivedItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<DeliveryInfo>) {
    adapter.setItems(items.filter { it.inbound == true }
      .map { it.items })
  }
}