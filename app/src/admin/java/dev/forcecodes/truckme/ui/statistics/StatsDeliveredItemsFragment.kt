package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.DeliveryInfo

class StatsDeliveredItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<DeliveryInfo>) {
    adapter.setItems(items.filter { it.inbound == false }
      .map { it.items })
  }
}
