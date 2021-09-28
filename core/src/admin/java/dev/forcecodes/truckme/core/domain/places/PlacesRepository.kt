package dev.forcecodes.truckme.core.domain.places

import dev.forcecodes.truckme.core.data.places.PlaceAutoCompleteResponse
import dev.forcecodes.truckme.core.data.places.PlaceDetailsResponse
import dev.forcecodes.truckme.core.util.ApiResponse
import dev.forcecodes.truckme.core.data.places.PlacesApiService
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.di.PlacesBackendApi
import dev.forcecodes.truckme.core.model.GeoCodeResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface PlacesRepository {
  fun getAutocompletePlaces(placeAddress: String): Flow<ApiResponse<PlaceAutoCompleteResponse>>
  fun getPlaceDetails(placedId: String): Flow<ApiResponse<PlaceDetailsResponse>>
  fun getReverseGeoCode(latLng: LatLng): Flow<ApiResponse<GeoCodeResponse>>
}

class PlacesRepositoryImpl @Inject constructor(
  @PlacesBackendApi private val apiService: PlacesApiService,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(ioDispatcher), PlacesRepository {

  override fun getAutocompletePlaces(
    placeAddress: String
  ): Flow<ApiResponse<PlaceAutoCompleteResponse>> =
    // do not remove explicit type for readability.
    flow { emit(getResult { apiService.getAutocomplete(placeAddress) }) }

  override fun getPlaceDetails(
    placedId: String
  ): Flow<ApiResponse<PlaceDetailsResponse>> =
    // do not remove explicit type for readability.
    flow { emit(getResult { apiService.getPlaceById(placedId) }) }

  override fun getReverseGeoCode(latLng: LatLng): Flow<ApiResponse<GeoCodeResponse>> {
    return flow { emit(getResult { apiService.getReverseGeoCode(latLng) }) }
  }
}


