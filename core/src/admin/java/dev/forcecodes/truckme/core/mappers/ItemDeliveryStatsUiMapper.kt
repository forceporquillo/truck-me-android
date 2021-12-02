package dev.forcecodes.truckme.core.mappers

import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.data.delivery.ItemMetaData
import dev.forcecodes.truckme.core.db.ItemDeliveredEntity
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.util.TimeUtils
import dev.forcecodes.truckme.core.util.TimeUtils.convertToTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemDeliveryStatsUiMapper @Inject constructor() :
  DomainMapperSingle<ItemDeliveredEntity, ItemDeliveredStats> {

  companion object {
    private const val DATE_FORMAT = "MM/dd/yyyy"
  }

  override suspend fun invoke(from: ItemDeliveredEntity): ItemDeliveredStats {
    return from.run {
      ItemDeliveredStats(
        documentId = documentId,
        itemTitle = items,
        timeStarted = startTimestamp.convertToTime(),
        timeCompleted = completedTimestamp.convertToTime(),
        estimatedTimeArrival = estimatedTimeDuration.convertToTime(),
        dateAccomplish = TimeUtils.convertToDate(timestampMillis = completedTimestamp),
        deliveryStatus = mapStatusDataEntity(from),
        metadata = ItemMetaData(
          completedTimestamp = completedTimestamp,
          driverName = driverName,
          bound = bound,
          date = TimeUtils.convertToDate(DATE_FORMAT, completedTimestamp)
        )
      )
    }
  }

  private fun mapStatusDataEntity(itemDeliveredEntity: ItemDeliveredEntity): String {
    if (itemDeliveredEntity.completedTimestamp == null || itemDeliveredEntity.estimatedTimeDuration == null) {
      return "Cannot be determined."
    }
    return if (itemDeliveredEntity.completedTimestamp < itemDeliveredEntity.estimatedTimeDuration) {
      "Delivered on time"
    } else {
      "Delivered late"
    }
  }
}