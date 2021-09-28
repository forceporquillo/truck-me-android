package dev.forcecodes.truckme.core.model

data class Places @JvmOverloads constructor(
  val placeId: String = "",
  val title: String? = "",
  val address: String?= ""
)