package dev.forcecodes.truckme.core.domain.places

import dev.forcecodes.truckme.core.data.places.PlaceAutoCompleteResponse
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.mapApiRequestResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
  private val placesApiRepository: PlacesRepository,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, PlaceAutoCompleteResponse>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<PlaceAutoCompleteResponse>> {
    return flow {
      emit(Result.Loading)
      val placesApiResponse = placesApiRepository
        .getAutocompletePlaces(parameters)
        .map { apiResponse ->
          apiResponse.mapApiRequestResults { "No results found." }
        }
      emitAll(placesApiResponse)
    }
  }
}
