package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.data.delivery.DeliveryDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetActiveJobsUseCase @Inject constructor(
  private val deliveryDataSource: DeliveryDataSource,
  private val activeJobDomainMapper: ActiveJobDomainMapper,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<DeliveryItems>>(dispatcher) {

  override fun execute(parameters: String): Flow<Result<List<DeliveryItems>>> {
    return deliveryDataSource.getActiveJobs(parameters).map { result ->
      if (result is Result.Success) {
        val deliverItems = result.data.map { activeJobDomainMapper.invoke(it) }
        Result.Success(deliverItems)
      } else {
        Result.Error(result.error)
      }
    }
  }
}