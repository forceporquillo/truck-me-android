package dev.forcecodes.truckme.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.AssignedDataSource
import dev.forcecodes.truckme.core.data.admin.AdminDataSource
import dev.forcecodes.truckme.core.data.directions.DirectionsResponse
import dev.forcecodes.truckme.core.data.directions.LegsItem
import dev.forcecodes.truckme.core.data.directions.Location
import dev.forcecodes.truckme.core.domain.directions.DirectionPath
import dev.forcecodes.truckme.core.domain.directions.GetDirectionsUseCase
import dev.forcecodes.truckme.core.domain.push.PushNotificationManager
import dev.forcecodes.truckme.core.fcm.MessageData
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.data
import dev.forcecodes.truckme.core.util.successOr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ActiveJobsViewModel @Inject constructor(
  private val assignedDataSource: AssignedDataSource,
  private val adminDataSource: AdminDataSource,
  private val getDirectionsUseCase: GetDirectionsUseCase,
  private val pushNotificationManager: PushNotificationManager
) : ViewModel() {

  private val _jobData = MutableStateFlow<DeliveryInfo?>(null)
  val jobData = _jobData.asStateFlow()

  private val _directions = MutableStateFlow<DirectionUiModel?>(null)
  val directions = _directions.asStateFlow()

  private val _currentLatLng = MutableStateFlow<LatLng?>(null)
  val currentLatLng = _currentLatLng.asStateFlow()

  private var latLngTruckMeImpl: LatLngTruckMeImpl? = null
  private var placeId: String? = null

  private val _endDirection = MutableStateFlow<LatLng?>(null)
  val endDirection = _endDirection.asStateFlow()

  private val _adminPhone = MutableStateFlow<String?>(null)
  val adminPhone = _adminPhone.asStateFlow()

  var deliveryTitle: String? = null
    private set

  private var shouldShow: Boolean = false

  private var jobId: String? = null

  fun getJob(jobId: String) {
    this.jobId = jobId

    viewModelScope.launch {
      Timber.e("Called $jobId")
      assignedDataSource.getJobById(jobId).collect { result ->
        getAdminMetaData(result.data?.assignedAdminId)
        if (result is Result.Success) {
          deliveryTitle = result.data.title
        }
        _jobData.value = result.successOr(DeliveryInfo())
      }
    }
  }

  private fun getAdminMetaData(adminId: String?) {
    viewModelScope.launch {
      adminDataSource.getAdminContactNumber(adminId).collect { result ->
        Timber.e(result.toString())
        if (result is Result.Success) {
          _adminPhone.value = result.data
        }
      }
    }
  }

  fun updateCurrentLocation(
    latLngTruckMeImpl: LatLngTruckMeImpl,
  ) {
    this.latLngTruckMeImpl = latLngTruckMeImpl
    this.placeId = jobData.value?.destination?.placeId
    val (lat, lng) = latLngTruckMeImpl
    _currentLatLng.value = LatLng(lat, lng)
  }

  fun reloadDirections() {
    if (latLngTruckMeImpl == null && placeId == null) {
      return
    }
    if (!shouldShow) {
      shouldShow = true
    }
    getDirections(latLngTruckMeImpl!!, placeId!!)
  }

  fun getDirections(
    latLngTruckMeImpl: LatLngTruckMeImpl,
    placeId: String
  ) {
    viewModelScope.launch {
      getDirectionsUseCase(DirectionPath(latLngTruckMeImpl, placeId)).collect { directionResponse ->
        if (directionResponse is Result.Success) {
          _directions.emit(filterData(directionResponse.data))
        }
        Timber.e(directionResponse.toString())
      }
    }
  }

  private fun filterData(data: DirectionsResponse): DirectionUiModel {
    val legs: LegsItem? = data.routes?.get(0)?.legs?.get(0)
    _endDirection.value = toLatLng(legs?.endLocation)
    return DirectionUiModel(
      distance = legs?.distance?.text ?: "Unavailable",
      duration = legs?.duration?.text ?: "Unavailable",
      durationInSeconds = legs?.duration?.value ?: 0,
      eta = legs?.duration?.text ?: "Unavailable",
      startLocation = toLatLng(legs?.startLocation),
      endLocation = toLatLng(legs?.endLocation),
      polyline = data.routes?.get(0)?.overviewPolyline?.points,
      shouldShowPath = shouldShow
    )
  }

  private fun toLatLng(startLocation: Location?): LatLng {
    return LatLng(startLocation?.lat ?: 0.0, startLocation?.lng ?: 0.0)
  }

  fun notifyAdmin() {
    viewModelScope.launch {
      jobData.value.let { deliveryInfo ->
        val adminToken = deliveryInfo?.assignedAdminTokenId ?: return@launch
        Timber.e("Notifying admin with token ID: $adminToken")

        val messageData = MessageData(
          deliveryInfo.title,
          deliveryInfo.items,
          deliveryInfo.id,
          deliveryInfo.driverData?.driverName!!
        )
        pushNotificationManager.notifyAdmin(adminToken, messageData)
      }
    }
  }
}