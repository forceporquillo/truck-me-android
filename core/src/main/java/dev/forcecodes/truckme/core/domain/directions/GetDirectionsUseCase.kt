package dev.forcecodes.truckme.core.domain.directions

import dev.forcecodes.truckme.core.api.DirectionsResponse
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetType.DRIVER
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Loading
import dev.forcecodes.truckme.core.util.mapApiRequestResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDirectionsUseCase @Inject constructor(
  private val directionsRepository: DirectionsRepository,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FlowUseCase<DirectionPath, DirectionsResponse>(dispatcher) {

  override fun execute(parameters: DirectionPath): Flow<Result<DirectionsResponse>> {
    return flow {
      emit(Loading)
      val directionsApiResponse = directionsRepository
        .getDirections(parameters)
        .map { directionsApiResponse ->
          directionsApiResponse.mapApiRequestResults { "Empty Api Response" }
        }
      emitAll(directionsApiResponse)
    }
  }
}

