package dev.forcecodes.truckme.core.domain.jobs

import dev.forcecodes.truckme.core.data.ActiveJobItems
import dev.forcecodes.truckme.core.data.AssignedDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.error
import dev.forcecodes.truckme.core.util.successOr
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignedJobsUseCase @Inject constructor(
  private val assignedDataSource: AssignedDataSource,
  private val assignedJobsMapper: AssignedJobsMapper,
  private val registeredDriverDataSource: RegisteredDriverDataSource,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<ActiveJobItems>>(dispatcher) {

  override fun execute(parameters: String): Flow<Result<List<ActiveJobItems>>> {
    return registeredDriverDataSource.getUUIDbyAuthId(parameters).flatMapConcat { result ->
      getAssignedDeliveries(result.successOr(""))
    }
  }

  private suspend fun getAssignedDeliveries(assignedId: String): Flow<Result<List<ActiveJobItems>>> {
    return assignedDataSource.getAssignedDeliveries(assignedId).map { result ->
      if (result is Result.Success) {
        val deliverItems = result.data
          .map {
            if (it.deliveryInfo == null) {
              ActiveJobItems("", "", "", "")
            } else {
              assignedJobsMapper.invoke(it.deliveryInfo)
            }
          }

        Result.Success(deliverItems)
      } else {
        Result.Error(result.error)
      }
    }
  }
}

