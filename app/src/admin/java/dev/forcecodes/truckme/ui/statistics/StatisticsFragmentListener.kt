package dev.forcecodes.truckme.ui.statistics

import dev.forcecodes.truckme.core.model.DeliveryInfo

interface StatisticsFragmentListener {
  fun onChangeSearch(items: List<DeliveryInfo>)
}
