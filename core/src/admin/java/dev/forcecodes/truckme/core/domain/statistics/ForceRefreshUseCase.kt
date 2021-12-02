package dev.forcecodes.truckme.core.domain.statistics

import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForceRefreshUseCase @Inject constructor(
  private val statisticsRepository: StatisticsRepository,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, Boolean>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<Boolean>> {
    return statisticsRepository.forceRefresh(parameters).map {
      Result.Success(it)
    }
  }
}