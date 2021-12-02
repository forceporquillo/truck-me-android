package dev.forcecodes.truckme.core.domain.fleets

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
import javax.inject.Singleton

@Singleton
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