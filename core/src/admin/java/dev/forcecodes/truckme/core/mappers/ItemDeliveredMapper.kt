package dev.forcecodes.truckme.core.mappers

import dev.forcecodes.truckme.core.data.delivery.DeliveredItemMetadata
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.data.delivery.ItemMetaData
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.TimeUtils.convertToDate
import dev.forcecodes.truckme.core.util.TimeUtils.convertToTime
import dev.forcecodes.truckme.core.util.TimeUtils.formatToDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemDeliveredMapper @Inject constructor() :
  DomainMapperSingle<DeliveredItemMetadata, ItemDeliveredStats> {

  companion object {
    private const val CANNOT_BE_DETERMINED = "Cannot be determined"
    private const val DELIVERED_ON_TIME = "Delivered on time"
    private const val DELIVERED_LATE = "Delivered late"
  }

  override suspend fun invoke(from: DeliveredItemMetadata): ItemDeliveredStats {
    return from.run {
      val deliveryInfo = from.deliveryInfo

      ItemDeliveredStats(
        documentId = documentId,
        itemTitle = deliveryInfo.items,
        timeStarted = deliveryInfo.startTimestamp.convertToTime(),
        timeCompleted = deliveryInfo.completedTimestamp.convertToTime(),
        estimatedTimeArrival = deliveryInfo.estimatedTimeDuration.convertToTime(),
        dateAccomplish = convertToDate(timestampMillis = deliveryInfo.completedTimestamp),
        deliveryStatus = mapStatus(deliveryInfo),
        metadata = ItemMetaData(
          completedTimestamp = deliveryInfo.completedTimestamp,
          driverName = deliveryInfo.driverData?.driverName,
          bound = deliveryInfo.inbound
            ?: throw IllegalStateException("Delivery state cannot be null."),
          date = formatToDate(deliveryInfo.completedTimestamp)
        )
      )
    }
  }

  private fun mapStatus(deliveryInfo: DeliveryInfo): String {
    if (deliveryInfo.completedTimestamp == null || deliveryInfo.estimatedTimeDuration == null) {
      return CANNOT_BE_DETERMINED
    }

    return if (deliveryInfo.completedTimestamp < deliveryInfo.estimatedTimeDuration) {
      DELIVERED_ON_TIME
    } else {
      DELIVERED_LATE
    }
  }
}