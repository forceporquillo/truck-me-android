package dev.forcecodes.truckme.core.model

data class LatLngTruckMeImpl(val lat: Double, val lng: Double) {
  override fun toString(): String {
    return String.format("%.6f,%.6f", lat, lng)
  }
}