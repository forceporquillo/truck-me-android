package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.then
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveJobDomainMapper @Inject constructor() :
  DomainMapperSingle<DeliveryInfo, DeliveryItems> {

  companion object {
    private const val UNAVAILABLE = "No data available or uncalibrated"
  }

  override suspend fun invoke(from: DeliveryInfo): DeliveryItems {
    return from.run {
      DeliveryItems(
        id = id,
        timeStamp = System.currentTimeMillis().toString(),
        driverName = title,
        driverId = driverData?.id ?: "",
        vehicleId = vehicleData?.id ?: "",
        destination = destination?.address ?: destination?.title ?: "",
        eta = eta.isNullOrEmpty() then UNAVAILABLE ?: "ETA: $eta",
        profileIcon = driverData?.profileUrl
      )
    }
  }
}