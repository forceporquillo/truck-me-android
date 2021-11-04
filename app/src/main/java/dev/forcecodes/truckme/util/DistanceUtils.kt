package dev.forcecodes.truckme.util

import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi

fun getTimeTaken(distance: Double): String {
  val kilometers = (distance / 1000)

  // base speed is 500 meters per minute
  val minutesTaken = 0.5

  val totalMinutes = (kilometers / minutesTaken).toInt()

  if (totalMinutes < 60) {
    return if (totalMinutes <= 1) {
      return "< 1 min"
    } else {
      "$totalMinutes mins"
    }
  }
  var minutes = (totalMinutes % 60).toString()
  minutes = if (minutes.length == 1) "0$minutes" else minutes
  return "${(totalMinutes / 60)} hour $minutes mins"
}

@RequiresApi(VERSION_CODES.N)
fun distanceLeft(distance: Double): String {
  return if (distance.toInt() > 1000) {
    "${MapUtils.roundDistance(distance)} km"
  } else {
    "${distance.toInt()} meters"
  }
}
