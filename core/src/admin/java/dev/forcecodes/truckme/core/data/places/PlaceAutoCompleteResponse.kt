package dev.forcecodes.truckme.core.data.places

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import dev.forcecodes.truckme.core.util.ApiResponseStatus

data class PlaceAutoCompleteResponse(
  @SerializedName("predictions")
  @Expose
  val predictions: List<Prediction>? = null,

  @SerializedName("status")
  @Expose
  override val status: String? = null,

  @SerializedName("error_message")

  @Expose
  val errorMessage: String? = null
) : ApiResponseStatus

data class Prediction(
  @SerializedName("description")
  @Expose
  var description: String? = null,

  @SerializedName("id")
  @Expose
  var id: String? = null,

  @SerializedName("place_id")
  @Expose
  var placeId: String? = null,

  @SerializedName("reference")
  @Expose
  var reference: String? = null,

  @SerializedName("structured_formatting")
  @Expose
  var structuredFormatting: StructuredFormatting? = null
)

data class StructuredFormatting(
  @SerializedName("main_text")
  @Expose
  var mainText: String? = null,

  @SerializedName("secondary_text")
  @Expose
  var secondaryText: String? = null
)