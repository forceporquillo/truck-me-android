package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.fleets.VehicleByteArray
import dev.forcecodes.truckme.core.util.TaskData

data class VehicleResult @JvmOverloads constructor(
  override var data: VehicleByteArray? = null,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<VehicleByteArray>