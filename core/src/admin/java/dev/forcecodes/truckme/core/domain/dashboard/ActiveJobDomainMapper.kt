package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveJobDomainMapper @Inject constructor() :
  DomainMapperSingle<DeliveryInfo, DeliveryItems> {
  override suspend fun invoke(from: DeliveryInfo): DeliveryItems {
    return from.run {
      DeliveryItems(
        id = id,
        timeStamp = System.currentTimeMillis().toString(),
        driverName = title,
        destination = destination?.address ?: destination?.title ?: "",
        eta = "ETA: Not yet implemented",
        profileIcon = driverData?.profileUrl
      )
    }
  }
}