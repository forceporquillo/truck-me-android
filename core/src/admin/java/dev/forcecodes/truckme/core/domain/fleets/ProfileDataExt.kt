package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.util.FleetDelegate

@FleetDelegate
fun String.getProfileData(
  vehicleId: String,
  profileInBytes: ByteArray
) = FleetProfileData(this, ProfileData(vehicleId, profileInBytes))
