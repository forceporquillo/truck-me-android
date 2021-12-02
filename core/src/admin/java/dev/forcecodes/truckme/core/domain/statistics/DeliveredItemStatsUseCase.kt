package dev.forcecodes.truckme.core.domain.statistics

import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.mappers.ItemDeliveryStatsUiMapper
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveredItemStatsUseCase @Inject constructor(
  private val statisticsRepository: StatisticsRepository,
  private val itemDeliveredUiMapper: ItemDeliveryStatsUiMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<QueryParams, List<ItemDeliveredStats>>(ioDispatcher) {

  override fun execute(parameters: QueryParams): Flow<Result<List<ItemDeliveredStats>>> {
    val (adminId, filterType, delegate) = parameters
    return statisticsRepository.getAllDeliveredItems(adminId).map { list ->
      val items = list.map { itemDeliveredEntity ->
        itemDeliveredUiMapper(itemDeliveredEntity)
      }

      Result.Success(
        if (filterType == DRIVER) {
          items.filter {
            it.metadata.driverName == delegate
          }
        } else {
          if (delegate == null) {
            items.filter {
              it.metadata.date == items.first().metadata.date
            }
          } else {
            items.filter {
              it.metadata.date == delegate
            }
          }
        }
      )
    }
  }
}

data class QueryParams(
  val adminId: String,
  @StatisticsSortType val filterType: String,
  val delegate: String?
)