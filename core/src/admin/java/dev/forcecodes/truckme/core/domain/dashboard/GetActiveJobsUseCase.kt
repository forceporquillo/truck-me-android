package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetActiveJobsUseCase @Inject constructor(
  private val deliveryDataSource: AdminDeliveryDataSource,
  private val activeJobDomainMapper: ActiveJobDomainMapper,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FlowUseCase<GetOrder, List<DeliveryItems>>(dispatcher) {

  override fun execute(parameters: GetOrder): Flow<Result<List<DeliveryItems>>> {
    return deliveryDataSource.getActiveJobsByOrder(parameters).map { result ->
      if (result is Result.Success) {
        val deliverItems = result.data.map { activeJobDomainMapper.invoke(it) }
        Result.Success(deliverItems)
      } else {
        Result.Error(result.error)
      }
    }
  }
}

enum class ActiveJobOder {
  IN_PROGRESS,
  PENDING
}

data class GetOrder(val uid: String, val order: ActiveJobOder)