package dev.forcecodes.truckme.core.domain.places

import dev.forcecodes.truckme.core.util.ApiResponseStatus
import dev.forcecodes.truckme.core.util.ApiResponse
import dev.forcecodes.truckme.core.util.ApiResponse.ApiEmptyResponse
import dev.forcecodes.truckme.core.util.ApiResponse.ApiErrorResponse
import dev.forcecodes.truckme.core.util.ApiResponse.ApiSuccessResponse
import dev.forcecodes.truckme.core.util.Result

fun <T: ApiResponseStatus> ApiResponse<T>.mapApiRequestResults(
  emptyApiResponseMessage: () -> String
): Result<T> {
  return when (this) {
    is ApiSuccessResponse<T> -> Result.Success(body)
    is ApiErrorResponse<T> -> Result.Error(Exception(errorMessage))
    is ApiEmptyResponse<T> -> Result.Error(Exception(emptyApiResponseMessage()))
  }
}