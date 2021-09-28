package dev.forcecodes.truckme.core.model

import com.google.gson.annotations.SerializedName
import dev.forcecodes.truckme.core.util.ApiResponseStatus

data class GeoCodeResponse(

	@field:SerializedName("results")
	val results: List<ResultsItem?>? = null,

	@field:SerializedName("status")
	override val status: String? = null

): ApiResponseStatus

data class Bounds(

	@field:SerializedName("southwest")
	val southwest: Southwest? = null,

	@field:SerializedName("northeast")
	val northeast: Northeast? = null
)

data class Location(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)

data class Southwest(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)

data class Viewport(

	@field:SerializedName("southwest")
	val southwest: Southwest? = null,

	@field:SerializedName("northeast")
	val northeast: Northeast? = null
)

data class ResultsItem(

	@field:SerializedName("formatted_address")
	val formattedAddress: String? = null,

	@field:SerializedName("geometry")
	val geometry: Geometry? = null,

	@field:SerializedName("place_id")
	val placeId: String? = null
)

data class Geometry(

	@field:SerializedName("bounds")
	val bounds: Bounds? = null,

	@field:SerializedName("location")
	val location: Location? = null,

)

data class Northeast(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)
