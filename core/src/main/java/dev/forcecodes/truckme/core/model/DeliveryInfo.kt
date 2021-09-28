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
  val coordinates: LatLngData? = null,
  val id: String = UUID.randomUUID().toString()
)