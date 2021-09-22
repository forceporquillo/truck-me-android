package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.util.FleetDelegate

data class FleetProfileData(
  @FleetDelegate val fleetType: String,
  val profileData: ProfileData
)