package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.retrievedAssignedFleetById
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveVehicleFleetsUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<VehicleUri>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<VehicleUri>>> {
    return fleetDataSource.observeVehicleChanges().map { result ->
      retrievedAssignedFleetById(result, parameters)
    }
  }
}
