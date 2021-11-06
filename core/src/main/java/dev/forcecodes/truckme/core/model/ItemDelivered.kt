package dev.forcecodes.truckme.core.model

data class ItemDelivered @JvmOverloads constructor(
  val deliveryInfo: DeliveryInfo? = null,
  val timestamp: String = System.currentTimeMillis().toString()
)