package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats

class StatsDeliveredItemsFragment : StatsPagerFragment() {

  override fun onChangeSearch(items: List<ItemDeliveredStats>) {
    adapter.setItems(items.filter { !it.metadata.bound })
  }
}
