package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.fleets.DriverByteArray
import dev.forcecodes.truckme.core.util.TaskData

data class DriverResult @JvmOverloads constructor(
  override var data: DriverByteArray? = null,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<DriverByteArray>