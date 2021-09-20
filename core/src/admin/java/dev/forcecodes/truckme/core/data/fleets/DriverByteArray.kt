package dev.forcecodes.truckme.core.data.fleets

import java.util.UUID

data class DriverByteArray(
  val fullName: String,
  val email: String,
  val password: String,
  val contact: String,
  override var profile: ByteArray? = null,
  override val id: String = UUID.randomUUID().toString(),
  override var isActive: Boolean = false,
  override val assignedAdminId: String
) : Fleets<ByteArray> {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DriverByteArray

    if (fullName != other.fullName) return false
    if (email != other.email) return false
    if (password != other.password) return false
    if (contact != other.contact) return false
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
    var result = fullName.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + password.hashCode()
    result = 31 * result + contact.hashCode()
    result = 31 * result + (profile?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + isActive.hashCode()
    result = 31 * result + assignedAdminId.hashCode()
    return result
  }
}