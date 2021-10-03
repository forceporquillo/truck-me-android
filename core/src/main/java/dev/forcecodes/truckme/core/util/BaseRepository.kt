package dev.forcecodes.truckme.core.util

import dev.forcecodes.truckme.core.util.ApiResponse.ApiEmptyResponse
import dev.forcecodes.truckme.core.util.ApiResponse.ApiErrorResponse
import dev.forcecodes.truckme.core.util.ApiResponse.ApiSuccessResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response

internal val errorResults = arrayOf("ZERO_RESULTS", "INVALID_REQUEST")

abstract class BaseRepository(private val dispatcher: CoroutineDispatcher) {
  protected suspend fun <T : ApiResponseStatus> getResult(
    call: suspend () -> Response<T>
  ): ApiResponse<T> = withContext(dispatcher) {
    try {
      val response = call()
      if (response.isSuccessful) {
        val body = response.body()
        if (body == null || response.code() == 204) {
          ApiEmptyResponse()
        } else if (body.status in errorResults) {
          ApiEmptyResponse()
        } else {
          ApiSuccessResponse(body)
        }
      } else {
        ApiErrorResponse(parseError(response))
      }
    } catch (e: Exception) {
      ApiErrorResponse(e.message.toString())
    }
  }

  private fun <T> parseError(response: Response<T>): String {
    val msg = response.errorBody()?.string()
    return (msg.isNullOrEmpty() then response.message() ?: msg) ?: "unknown error"
  }
}