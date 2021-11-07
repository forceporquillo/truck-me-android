package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.convertToDate
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.retrievedAssignedFleetById
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            timeStampMillis = deliveredItem.timestamp
          ) ?: ""
        }
      )
    }
  }
}