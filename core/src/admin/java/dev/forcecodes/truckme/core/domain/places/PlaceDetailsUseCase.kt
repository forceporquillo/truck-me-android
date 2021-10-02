package dev.forcecodes.truckme.core.domain.places

import dev.forcecodes.truckme.core.data.places.PlaceDetailsResponse
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Loading
import dev.forcecodes.truckme.core.util.mapApiRequestResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaceDetailsUseCase @Inject constructor(
  private val placesApiRepository: PlacesRepository,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, PlaceDetailsResponse>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<PlaceDetailsResponse>> {
    return flow {
      emit(Loading)
      val placesApiResponse = placesApiRepository
        .getPlaceDetails(parameters)
        .map { placesApiResponse ->
          placesApiResponse.mapApiRequestResults { "Invalid request type." }
        }
      emitAll(placesApiResponse)
    }
  }
}