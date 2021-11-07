package dev.forcecodes.truckme.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.api.DirectionsResponse
import dev.forcecodes.truckme.core.api.LegsItem
import dev.forcecodes.truckme.core.api.Location
import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.db.Notification
import dev.forcecodes.truckme.core.domain.directions.DirectionPath
import dev.forcecodes.truckme.core.domain.directions.GetDirectionsUseCase
import dev.forcecodes.truckme.core.domain.notification.NotificationManager
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.util.DirectionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ActiveJobsViewModel @Inject constructor(
  private val getDirectionsUseCase: GetDirectionsUseCase,
  private val deliveryDataSource: AdminDeliveryDataSource,
  private val notificationManager: NotificationManager
) : ViewModel() {

  private val _deliveryInfo = MutableStateFlow<DeliveryInfo?>(null)
  val deliveryInfo = _deliveryInfo.asStateFlow()

  private val _directions = MutableStateFlow<DirectionUiModel?>(null)
  val directions = _directions.asStateFlow()

  private var id: String? = null


  init {
    viewModelScope.launch {
      deliveryInfo.collect {
        if (it == null) {
          return@collect
        }

        val (lat, lng) = it.startDestination ?: return@collect
        getDirections(LatLngTruckMeImpl(lat ?: 0.0, lng ?: 0.0), it.destination?.placeId ?: "")
      }
    }
  }

  fun getJob(id: String) {
    this.id = id
    viewModelScope.launch {
      deliveryDataSource.getActiveJobById(id).collect {
        _deliveryInfo.value = it.successOr(null)
      }
    }
  }

  fun notifyWhenDelivered() {
    notificationManager.setNotification(Notification(id ?: return))
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

    return DirectionUiModel(
      distance = legs?.distance?.text ?: "Unavailable",
      duration = legs?.duration?.text ?: "Unavailable",
      durationInSeconds = legs?.duration?.value ?: 0,
      eta = legs?.duration?.text ?: "Unavailable",
      startLocation = toLatLng(legs?.startLocation),
      endLocation = toLatLng(legs?.endLocation),
      polyline = data.routes?.get(0)?.overviewPolyline?.points,
      shouldShowPath = true
    )
  }

  private fun toLatLng(startLocation: Location?): LatLng {
    return LatLng(startLocation?.lat ?: 0.0, startLocation?.lng ?: 0.0)
  }
}
