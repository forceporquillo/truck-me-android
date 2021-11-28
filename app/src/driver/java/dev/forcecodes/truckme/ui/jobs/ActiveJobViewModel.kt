package dev.forcecodes.truckme.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.api.DirectionsResponse
import dev.forcecodes.truckme.core.api.LegsItem
import dev.forcecodes.truckme.core.api.Location
import dev.forcecodes.truckme.core.data.AssignedDataSource
import dev.forcecodes.truckme.core.data.DeliveryInfoMetaData
import dev.forcecodes.truckme.core.data.UpdateDeliveryDataSource
import dev.forcecodes.truckme.core.data.admin.AdminDataSource
import dev.forcecodes.truckme.core.domain.directions.DirectionPath
import dev.forcecodes.truckme.core.domain.directions.GetDirectionsUseCase
import dev.forcecodes.truckme.core.domain.fleets.UpdateDriverFleetStateUseCase
import dev.forcecodes.truckme.core.domain.fleets.UpdateVehicleFleetStateUseCase
import dev.forcecodes.truckme.core.domain.push.PushNotificationManager
import dev.forcecodes.truckme.core.fcm.MessageData
import dev.forcecodes.truckme.core.model.LatLngData
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.data
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.util.DirectionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ActiveJobsViewModel @Inject constructor(
  private val assignedDataSource: AssignedDataSource,
  private val adminDataSource: AdminDataSource,
  private val getDirectionsUseCase: GetDirectionsUseCase,
  private val updateDriverFleetStateUseCase: UpdateDriverFleetStateUseCase,
  private val updateVehicleFleetStateUseCase: UpdateVehicleFleetStateUseCase,
  private val pushNotificationManager: PushNotificationManager,
  private val updateDeliveryDataSource: UpdateDeliveryDataSource
) : ViewModel() {

  private val _jobData = MutableStateFlow<DeliveryInfoMetaData?>(null)
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

  var estimatedArrivalTime: String? = null

  var durationLess: String? = null
    set(value) {
      value?.let { approx ->
        val documentId = jobData.value?.documentId
        updateDeliveryDataSource.duration(approx, documentId ?: return)
      }
      field = value
    }

  var distanceRemainingApprox: String? = null
    set(value) {
      value?.let { approx ->
        val documentId = jobData.value?.documentId
        updateDeliveryDataSource.distanceRemainingApprox(approx, documentId ?: return)
      }
      field = value
    }

  private var jobId: String? = null

  private var durationInSeconds: Int? = null // eta

  var startDestination: LatLngData? = null

  init {
    viewModelScope.launch {
      jobData.map { it?.deliveryInfo?.startDestination }.collect {
        if (it != null) {
          Timber.e("Item Delivery has started...")
        } else {
          Timber.e("Item Delivery has not started...")
        }
      }
    }
  }

  var distanceRemaining: String? = null
    set(value) {
      Timber.e("Distance Remaining $value")
      field = value
    }

  fun getJob(jobId: String) {
    this.jobId = jobId

    viewModelScope.launch {
      assignedDataSource.getJobById(jobId).collect { result ->
        val deliveryInfo = result.data?.deliveryInfo
        getAdminMetaData(deliveryInfo?.assignedAdminId)
        if (result is Result.Success) {
          deliveryTitle = deliveryInfo?.title
        }
        _jobData.value = result.successOr(null)
      }
    }
  }

  private fun getAdminMetaData(adminId: String?) {
    viewModelScope.launch {
      adminDataSource.getAdminContactNumber(adminId).collect { result ->
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
    val deliveryInfo = jobData.value?.deliveryInfo
    this.placeId = deliveryInfo?.destination?.placeId
    val (lat, lng) = latLngTruckMeImpl
    _currentLatLng.value = LatLng(lat, lng)
  }

  fun startAndReload() {
    if (latLngTruckMeImpl == null && placeId == null) {
      return
    }
    if (!shouldShow) {
      shouldShow = true
    }
    getDirections(latLngTruckMeImpl!!, placeId!!)
    startDelivery()
  }

  fun getDirections(
    latLngTruckMeImpl: LatLngTruckMeImpl,
    placeId: String
  ) {
    viewModelScope.launch {
      getDirectionsUseCase(DirectionPath(latLngTruckMeImpl, placeId)).collect { directionResponse ->
        if (directionResponse is Result.Success) {
          _directions.value = filterData(directionResponse.data)
        }
        Timber.e(directionResponse.toString())
      }
    }
  }

  private fun filterData(data: DirectionsResponse): DirectionUiModel {
    val legs: LegsItem? = data.routes?.get(0)?.legs?.get(0)
    _endDirection.value = toLatLng(legs?.endLocation)

    durationInSeconds = legs?.duration?.value ?: 0

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

  private fun startDelivery() {
    val documentId = jobData.value?.documentId

    if (documentId.isNullOrEmpty()) {
      return
    }
    updateDeliveryDataSource.onStartDelivery(documentId)

    viewModelScope.launch {
      launch { setDriverOnGoingDelivery(true) }
      launch { updateCoordinates(documentId) }
      launch { updateRemainingDistance(documentId) }
      launch { updateStartDestination(documentId) }
      launch { updateArrival(documentId) }
      launch { updateArrivalTime(documentId) }
    }
  }

  private fun updateArrivalTime(documentId: String) {
    val eta = durationInSeconds ?: return

    val now = Calendar.getInstance()
    now.add(Calendar.SECOND, eta)

    updateDeliveryDataSource.estimatedArrivalTime(now.timeInMillis, documentId)
  }

  private fun updateStartDestination(documentId: String) {
    startDestination?.let {
      Timber.e("Start Destination $it")
      updateDeliveryDataSource.startDestination(it, documentId)
    }
  }

  private suspend fun updateCoordinates(documentId: String) {
    currentLatLng.collect {
      Timber.e("updating....")
      updateDeliveryDataSource.updateCurrentLocation(
        LatLngData(it?.latitude, it?.longitude),
        documentId
      )
    }
  }

  private fun updateRemainingDistance(documentId: String) {
    if (!distanceRemaining.isNullOrEmpty() || !documentId.isEmpty()) {
      updateDeliveryDataSource.distanceRemaining(distanceRemaining!!, documentId)
    }
  }

  private suspend fun setDriverOnGoingDelivery(available: Boolean) {
    jobData.collect {
      it?.deliveryInfo?.driverData?.id?.let { driverId ->
        updateDriverFleetStateUseCase(Pair(driverId, available))
      }
      it?.deliveryInfo?.vehicleData?.id?.let { vehicleId ->
        updateVehicleFleetStateUseCase(Pair(vehicleId, available))
      }
    }
  }

  private fun toLatLng(startLocation: Location?): LatLng {
    return LatLng(startLocation?.lat ?: 0.0, startLocation?.lng ?: 0.0)
  }

  fun notifyAdmin() {
    confirmDelivery()
    viewModelScope.launch {
      jobData.value.let { metadata ->
        val deliveryInfo = metadata?.deliveryInfo
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

  private fun confirmDelivery() {
    updateDeliveryDataSource.onFinishDelivery(jobData.value?.documentId ?: return)
    viewModelScope.launch {
      setDriverOnGoingDelivery(false)
    }
  }

  private fun updateArrival(documentId: String) {
    if (!estimatedArrivalTime.isNullOrEmpty()) {
      val etaApprox = estimatedArrivalTime?.replace("<", "")
      etaApprox?.let { updateDeliveryDataSource.arrivalTime(it, documentId) }
    }
  }
}