package dev.forcecodes.truckme.core.domain.places

import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.model.GeoCodeResponse
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Loading
import dev.forcecodes.truckme.core.util.mapApiRequestResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReverseGeoCoordinateUseCase @Inject constructor(
  private val placesApiRepository: PlacesRepository,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<LatLng, GeoCodeResponse>(ioDispatcher) {

  override fun execute(parameters: LatLng): Flow<Result<GeoCodeResponse>> {
    return flow {
      emit(Loading)
      val placesApiResponse = placesApiRepository
        .getReverseGeoCode(parameters)
        .map { apiResponse ->
          apiResponse.mapApiRequestResults { "No results found." }
        }
      emitAll(placesApiResponse)
    }
  }
}

data class LatLng(val lat: Double, val lng: Double) {
  override fun toString(): String {
    return String.format("%.6f,%.6f", lat, lng)
  }
}
