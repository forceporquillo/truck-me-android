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
  var isActive: Boolean = true,
  val coordinates: Coordinates? = null,
  val currentCoordinates: LatLngData? = null,
  val assignedAdminId: String = "",
  val assignedAdminTokenId: String? = null,
  val id: String = UUID.randomUUID().toString()
)

data class Coordinates @JvmOverloads constructor(
  val startDestination: LatLngData? = null,
  val currentDestination: LatLngData? = null,
  val finalDestination: LatLngData? = null
)