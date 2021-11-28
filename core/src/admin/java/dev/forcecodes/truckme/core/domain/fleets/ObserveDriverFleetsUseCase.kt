package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemMetadata
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.data.delivery.ItemMetaData
import dev.forcecodes.truckme.core.data.delivery.convertToDate
import dev.forcecodes.truckme.core.data.delivery.convertToTime
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.retrievedAssignedFleetById
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.IllegalStateException
import javax.inject.Inject

class ObserveDriverFleetsUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<DriverUri>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<DriverUri>>> {
    return fleetDataSource.observeDriverChanges().map { result ->
      retrievedAssignedFleetById(result, parameters)
    }
  }
}

class DailyStatisticsUseCase @Inject constructor(
  private val deliveredItemDataSource: DeliveredItemDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<String>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<String>>> {
    return deliveredItemDataSource.getDailyStats(parameters).map { list ->
      Result.Success(
        list.map { deliveredItem ->
          convertToDate(
            "MM/dd/yyyy",
            timeStampMillis = deliveredItem.deliveryInfo.completedTimestamp
          ) ?: ""
        }
      )
    }
  }
}

class DeliveredItemStatsUseCase @Inject constructor(
  private val deliveredItemDataSource: DeliveredItemDataSource,
  private val itemDeliveredMapper: ItemDeliveredMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<ItemDeliveredStats>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<ItemDeliveredStats>>> {
    return deliveredItemDataSource.getAllDeliveredItems(parameters).map {
      Result.Success(
        it.map { metadata ->
          itemDeliveredMapper(metadata)
        }
      )
    }
  }
}

class ItemDeliveredMapper @Inject constructor() :
  DomainMapperSingle<DeliveredItemMetadata, ItemDeliveredStats> {

  companion object {
    private const val DATE_FORMAT = "MM/dd/yyyy"
  }

  override suspend fun invoke(from: DeliveredItemMetadata): ItemDeliveredStats {
    return from.run {
      val deliveryInfo = from.deliveryInfo

      ItemDeliveredStats(
        documentId = documentId,
        itemTitle = deliveryInfo.title,
        timeStarted = deliveryInfo.startTimestamp.convertToTime(),
        timeCompleted = deliveryInfo.completedTimestamp.convertToTime(),
        estimatedTimeArrival = deliveryInfo.estimatedTimeDuration.convertToTime(),
        dateAccomplish = convertToDate(timeStampMillis = deliveryInfo.completedTimestamp),
        deliveryStatus = mapStatus(deliveryInfo),
        metadata = ItemMetaData(
          completedTimestamp = deliveryInfo.completedTimestamp,
          driverName = deliveryInfo.driverData?.driverName,
          bound = deliveryInfo.inbound ?: throw IllegalStateException("Delivery state cannot be null."),
          date = convertToDate(DATE_FORMAT, deliveryInfo.completedTimestamp)
        )
      )
    }
  }

  private fun mapStatus(deliveryInfo: DeliveryInfo): String {
    if (deliveryInfo.completedTimestamp == null || deliveryInfo.estimatedTimeDuration == null) {
      return "Cannot be determined."
    }
    return if (deliveryInfo.completedTimestamp < deliveryInfo.estimatedTimeDuration) {
      "Delivered on time"
    } else {
      "Delivered late"
    }
  }

  private fun Long?.convertToTime(): String? {
    return convertToTime(this)
  }
}
