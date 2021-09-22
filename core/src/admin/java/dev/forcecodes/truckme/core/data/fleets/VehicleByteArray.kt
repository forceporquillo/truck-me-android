package dev.forcecodes.truckme.core.data.fleets

import java.util.UUID

data class VehicleByteArray @JvmOverloads constructor(
  val name: String? = "",
  val plate: String? = "",
  val description: String? = "",
  override var profile: ByteArray? = null,
  override val id: String = UUID.randomUUID().toString(),
  override var isActive: Boolean = false,
  override val assignedAdminId: String
) : Fleets<ByteArray> {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as VehicleByteArray

    if (name != other.name) return false
    if (plate != other.plate) return false
    if (description != other.description) return false
    if (profile != null) {
      if (other.profile == null) return false
      if (!profile.contentEquals(other.profile)) return false
    } else if (other.profile != null) return false
    if (id != other.id) return false
    if (isActive != other.isActive) return false
    if (assignedAdminId != other.assignedAdminId) return false
    return true
  }

  override fun hashCode(): Int {
    var result = name?.hashCode() ?: 0
    result = 31 * result + (plate?.hashCode() ?: 0)
    result = 31 * result + (description?.hashCode() ?: 0)
    result = 31 * result + (profile?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + isActive.hashCode()
    result = 31 * result + assignedAdminId.hashCode()
    return result
  }
}