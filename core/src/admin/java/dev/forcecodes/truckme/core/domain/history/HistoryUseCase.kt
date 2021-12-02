package dev.forcecodes.truckme.core.domain.history

import dev.forcecodes.truckme.core.data.delivery.DeliveredItem
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemMapper
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryUseCase @Inject constructor(
  private val deliveredItemDataSource: DeliveredItemDataSource,
  private val deliveredItemMapper: DeliveredItemMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<DeliveredItem>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<DeliveredItem>>> {
    return deliveredItemDataSource.getAllDeliveredItems(parameters).map { list ->
      val mappedList = list
        .sortedByDescending { deliveryInfo ->
          deliveryInfo.deliveryInfo.completedTimestamp
        }
        .map { itemDelivered ->
          deliveredItemMapper(itemDelivered.deliveryInfo)
        }
      Result.Success(mappedList)
    }
  }
}