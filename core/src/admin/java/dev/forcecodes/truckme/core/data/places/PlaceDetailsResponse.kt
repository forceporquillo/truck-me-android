package dev.forcecodes.truckme.core.data.places

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import dev.forcecodes.truckme.core.model.Geometry
import dev.forcecodes.truckme.core.util.ApiResponseStatus

data class PlaceDetailsResponse(
  @SerializedName("result")
  @Expose
  val result: Result? = null,

  @SerializedName("status")
  @Expose
  override val status: String? = null
) : ApiResponseStatus

data class Result(
  @SerializedName("formatted_address")
  @Expose
  var formattedAddress: String? = null,

  @SerializedName("geometry")
  @Expose
  var geometry: Geometry? = null,

  @SerializedName("id")
  @Expose
  var id: String? = null,

  @SerializedName("name")
  @Expose
  var name: String? = null,

  @SerializedName("place_id")
  @Expose
  var placeId: String? = null,

  @SerializedName("reference")
  @Expose
  var reference: String? = null
)