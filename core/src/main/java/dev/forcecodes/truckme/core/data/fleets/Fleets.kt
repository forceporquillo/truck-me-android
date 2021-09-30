package dev.forcecodes.truckme.core.data.fleets

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface Fleets<T> {
  val id: String
  var isActive: Boolean
  var profile: T?
  val assignedAdminId: String
}

interface FleetDelegate {
  var profile: String?
  val id: String
  var isActive: Boolean
  val assignedAdmin: String
}

sealed class FleetUiModel: FleetDelegate {

  @Parcelize
  data class VehicleUri @JvmOverloads constructor(
    val name: String = "",
    val plate: String = "",
    val description: String = "",
    override var profile: String? = "",
    override val id: String = "",
    override var isActive: Boolean = false,
    override val assignedAdmin: String = ""
  ) : FleetUiModel(), Parcelable

  @Parcelize
  data class DriverUri @JvmOverloads constructor(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val contact: String = "",
    override var profile: String? = "",
    override val id: String = "",
    override var isActive: Boolean = false,
    override val assignedAdmin: String = ""
  ) : FleetUiModel(), Parcelable
}