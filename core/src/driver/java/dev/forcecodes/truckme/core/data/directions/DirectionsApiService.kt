package dev.forcecodes.truckme.core.data.directions

import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {

  @GET("/maps/api/directions/json")
  suspend fun getDirections(
    @Query("mode") mode: String = "transit",
    @Query("origin") latLngTruckMeImplData: LatLngTruckMeImpl,
    @Query("destination") placeId: String,
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY,
  ): Response<DirectionsResponse>
}