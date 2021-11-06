package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateFleetStateUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<FleetStateUpdateMetadata, Unit>(ioDispatcher) {

  override suspend fun execute(parameters: FleetStateUpdateMetadata) {
    val (id, state, type) = parameters
    fleetDataSource.onUpdateFleetState(id, state, type)
  }
}

data class FleetStateUpdateMetadata(
  val id: String,
  val state: Boolean,
  val fleetType: FleetType
)