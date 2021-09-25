package dev.forcecodes.truckme.ui.fleet

sealed class FleetUploadState {
  object Success : FleetUploadState()
  object Loading : FleetUploadState()
  data class Error(val exception: Exception?) : FleetUploadState()
}