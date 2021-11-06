package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.ItemDelivered

interface StatisticsFragmentListener {
  fun onChangeSearch(items: List<ItemDelivered>)
}
