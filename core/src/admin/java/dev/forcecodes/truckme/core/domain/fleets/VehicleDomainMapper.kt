package dev.forcecodes.truckme.core.domain.fleets

import android.net.Uri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.data.fleets.VehicleByteArray
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.mapper.DomainMapperDouble
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleDomainMapper @Inject constructor(
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DomainMapperDouble<VehicleByteArray, Uri?, VehicleUri> {

  override suspend fun invoke(
    from: VehicleByteArray,
    param: Uri?
  ): VehicleUri {
    return withContext(dispatcher) {
      from.run {
        VehicleUri(
          id = id,
          name = name!!,
          plate = plate!! ,
          description = description ?: "",
          profile = param.toString(),
          isActive = isActive,
          assignedAdmin = from.assignedAdminId
        )
      }
    }
  }
}