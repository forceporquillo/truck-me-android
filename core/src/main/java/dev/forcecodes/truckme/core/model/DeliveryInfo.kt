package dev.forcecodes.truckme.core.model

import java.util.UUID

data class DeliveryInfo @JvmOverloads constructor(
  val title: String = "",
  val destination: Places? = null,
  val driverData: DriverData? = null,
  val vehicleData: VehicleData? = null,
  val items: String = "",
  val contactNumber: String = "",
  val inbound: Boolean? = null,
  var active: Boolean = true,
  val startDestination: LatLngData? = null,
  val currentCoordinates: LatLngData? = null,
  val finalDestination: LatLngData? = null,
  val assignedAdminId: String = "",
  val assignedAdminTokenId: String? = null,
  val completed: Boolean = false,
  val eta: String? = "",
  val distanceRemaining: String? = "",
  val distanceRemApprox: String? = null,
  val started: Boolean = false,
  val duration: String = "",
  val startTimestamp: Long? = null,
  val completedTimestamp: Long? = null,
  val estimatedTimeDuration: Long? = null,
  val id: String = UUID.randomUUID().toString()
)