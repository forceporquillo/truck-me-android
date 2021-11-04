package dev.forcecodes.truckme.ui.jobs

import com.google.android.gms.maps.model.LatLng

data class DirectionUiModel(
  val distance: String = "NA",
  val duration: String = "NA",
  val durationInSeconds: Int = 0,
  val eta: String = "NA",
  val startLocation: LatLng?,
  val endLocation: LatLng?,
  val polyline: String?,
  val shouldShowPath: Boolean = false
)