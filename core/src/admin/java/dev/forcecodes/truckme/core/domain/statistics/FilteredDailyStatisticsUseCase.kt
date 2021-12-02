package dev.forcecodes.truckme.core.domain.statistics

import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilteredDailyStatisticsUseCase @Inject constructor(
  private val statisticsRepository: StatisticsRepository,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<@StatisticsSortType String, List<String>>(ioDispatcher) {

  override fun execute(@StatisticsSortType parameters: String): Flow<Result<List<String>>> {
    return statisticsRepository.getCacheItems().map { list ->
      Result.Success(
        list.map { itemDeliveredEntity ->
          if (parameters == DAILY) {
            val timestamp = itemDeliveredEntity.completedTimestamp
            TimeUtils.formatToDate(timestamp)
          } else {
            itemDeliveredEntity.driverName.orEmpty()
          }
        }.sortedDescending()
          .distinct()
      )
    }
  }
}


