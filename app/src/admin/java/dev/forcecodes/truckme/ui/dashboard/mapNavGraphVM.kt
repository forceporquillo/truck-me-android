package dev.forcecodes.truckme.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.ui.dashboard.MapDeliverySharedViewModel

fun Fragment.mapNavGraphViewModels() =
  hiltNavGraphViewModels<MapDeliverySharedViewModel>(R.id.map_nav_graph)