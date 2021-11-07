package dev.forcecodes.truckme.core.domain.directions

import dev.forcecodes.truckme.core.api.DirectionsApiService
import dev.forcecodes.truckme.core.api.DirectionsResponse
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.util.ApiResponse
import dev.forcecodes.truckme.core.util.BaseRepository
import dev.forcecodes.truckme.core.util.DirectionsBackendApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface DirectionsRepository {
  fun getDirections(
    path: DirectionPath
  ): Flow<ApiResponse<DirectionsResponse>>
}

class DirectionsRepositoryImpl @Inject constructor(
  @DirectionsBackendApi private val directionsApiService: DirectionsApiService,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : BaseRepository(dispatcher), DirectionsRepository {

  override fun getDirections(
    path: DirectionPath
  ) = flow {
    val directionsResult =
      directionsApiService.getDirections(
        latLngTruckMeImplData = path.origin,
        placeId = "place_id:${path.destinationPlaceId}"
      )
    emit(getResult { directionsResult })
  }
}

data class DirectionPath(
  val origin: LatLngTruckMeImpl,
  val destinationPlaceId: String
)