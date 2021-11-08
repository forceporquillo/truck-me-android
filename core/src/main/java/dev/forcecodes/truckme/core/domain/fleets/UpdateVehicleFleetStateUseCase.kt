package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetType.VEHICLE
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateVehicleFleetStateUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<Pair<String, Boolean>, Unit>(dispatcher) {

  override suspend fun execute(parameters: Pair<String, Boolean>) {
    val (id, state) = parameters
    fleetDataSource.onUpdateFleetState(id, state, VEHICLE)
  }
}