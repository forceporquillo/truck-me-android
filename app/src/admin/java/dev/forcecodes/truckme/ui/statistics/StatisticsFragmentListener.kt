package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats

interface StatisticsFragmentListener {
  fun onChangeSearch(items: List<ItemDeliveredStats>)
}
