package dev.forcecodes.truckme.core.data.places

import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.model.GeoCodeResponse
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

  @GET("/maps/api/place/autocomplete/json")
  suspend fun getAutocomplete(
    @Query("input") input: String?,
    @Query("components") component: String = "country:ph",
    @Query("radius") radius: String = "5000",
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
  ): Response<PlaceAutoCompleteResponse>

  @GET("/maps/api/place/details/json")
  suspend fun getPlaceById(
    @Query("place_id") id: String?,
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
  ): Response<PlaceDetailsResponse>

  @GET("/maps/api/geocode/json")
  suspend fun getReverseGeoCode(
    @Query("latlng") latLng: LatLngTruckMeImpl?,
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
  ): Response<GeoCodeResponse>
}

