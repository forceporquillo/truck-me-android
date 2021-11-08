package dev.forcecodes.truckme.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.TruckMeApplication
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.data.places.PlaceDetailsResponse
import dev.forcecodes.truckme.core.domain.dashboard.AddDeliveryUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveVehicleFleetsUseCase
import dev.forcecodes.truckme.core.domain.places.PlaceDetailsUseCase
import dev.forcecodes.truckme.core.domain.places.ReverseGeoCoordinateUseCase
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.DriverData
import dev.forcecodes.truckme.core.model.LatLngData
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.model.Places
import dev.forcecodes.truckme.core.model.VehicleData
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.cancelIfActive
import dev.forcecodes.truckme.core.util.getAdminToken
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapDeliverySharedViewModel @Inject constructor(
  private val observeVehicleFleetsUseCase: ObserveVehicleFleetsUseCase,
  private val observeDriverFleetsUseCase: ObserveDriverFleetsUseCase,
  private val reverseGeoCoordinateUseCase: ReverseGeoCoordinateUseCase,
  private val addDeliveryUseCase: AddDeliveryUseCase,
  private val placeDetailsUseCase: PlaceDetailsUseCase,
  signInViewModelDelegate: SignInViewModelDelegate,
  application: Application
) : AndroidViewModel(application),
  SignInViewModelDelegate by signInViewModelDelegate {

  private val adminToken by lazy {
    getApplication<TruckMeApplication>().getAdminToken()
  }

  // region state flows
  private val _destinationAddress = MutableStateFlow<Places?>(null)
  val destinationAddress = _destinationAddress.asStateFlow()

  private val _vehicleList = MutableStateFlow<List<VehicleUri>>(emptyList())
  val vehicleList = _vehicleList.asStateFlow()

  private val _driverList = MutableStateFlow<List<DriverUri>>(emptyList())
  val driverList = _driverList

  private val _placeCoordinates = MutableStateFlow<AnimatedLatLng?>(null)
  val placeCoordinates = _placeCoordinates.asStateFlow()

  private val _isMapReady = MutableStateFlow(false)
  val isMapReady = _isMapReady.asStateFlow()

  private val _isDestinationAdded = MutableStateFlow(false)
  val isDestinationAdded = _isDestinationAdded.asStateFlow()

  private val _isReverseSearchLoading = MutableStateFlow(false)
  val isReverseSearchLoading = _isReverseSearchLoading.asStateFlow()

  private val _showMarker = MutableStateFlow(false)
  val showMarker = _showMarker.asStateFlow()

  private val _enableSubmitButton = MutableStateFlow(false)
  val enableSubmitButton = _enableSubmitButton.asStateFlow()

  private val _submitDeliveryUiEvent = MutableStateFlow(SubmitUiEvent())
  val submitDeliveryUiEvent = _submitDeliveryUiEvent.asStateFlow()

  private val _title = MutableStateFlow("")
  private val _address = MutableStateFlow("")
  private val _freightItem = MutableStateFlow("")
  private val _contact = MutableStateFlow("")
  private val _driver = MutableStateFlow<DriverData?>(null)
  private val _vehicle = MutableStateFlow<VehicleData?>(null)
  private val _boundDelivery = MutableStateFlow<Boolean?>(null)

  private val _latLng = MutableStateFlow<LatLngTruckMeImpl?>(null)

  var isLocationSet = false

  // endregion
  var isCleared = false
    private set

  private var searchJob: Job? = null

  private var interceptByUserGesture = false

  init {
    viewModelScope.launch {
      launch {
        userInfo.collect { authenticatedInfo ->
          loadAddedFleets(
            authenticatedInfo?.getUid()
              ?: throw RuntimeException("yes daddy")
          )
        }
      }
      launch {
        combine(
          _title, _address, _driver, _contact,
          _vehicle, _freightItem, _boundDelivery
        ) { info ->
          info.filterIsInstance<String?>().all { !it.isNullOrEmpty() }
            && (info[2] as? DriverData) != null && (info[4] as? VehicleData) != null
            && (info.last() as? Boolean) != null
        }.collect { enable -> _enableSubmitButton.value = enable }
      }
    }
  }

  data class SubmitUiEvent(
    var isLoading: Boolean = false,
    var isSuccess: Boolean = false,
    var exception: Exception? = null
  )

  fun submit() {
    val animatedLatLng = _placeCoordinates.value
    val (_, latLng) = animatedLatLng ?: return

    addDeliverySchedule(latLng)
  }

  private fun addDeliverySchedule(latLng: LatLng) {
    val deliveryInfo = DeliveryInfo(
      _title.value,
      _destinationAddress.value!!,
      _driver.value,
      _vehicle.value,
      _freightItem.value,
      _contact.value,
      _boundDelivery.value,
      active = false,
      assignedAdminId = userIdValue!!,
      assignedAdminTokenId = adminToken,
      finalDestination = LatLngData(latLng.latitude, latLng.longitude)
    )

    viewModelScope.launch {
      addDeliveryUseCase(deliveryInfo).collect { result ->
        _submitDeliveryUiEvent.value= when(result) {
          is Result.Loading -> SubmitUiEvent(true)
          is Result.Success -> SubmitUiEvent(false, result.data.isSuccess)
          is Result.Error -> SubmitUiEvent(exception = result.exception)
        }
      }
    }
  }

  private fun loadAddedFleets(adminId: String) {
    viewModelScope.launch {
      launch {
        observeVehicleFleetsUseCase(adminId).collect { result ->
          _vehicleList.mapFleetList(result)
        }
      }
      launch {
        observeDriverFleetsUseCase(adminId).collect { result ->
          _driverList.mapFleetList(result)
        }
      }
    }
  }

  private fun <T : FleetUiModel> MutableStateFlow<List<T>>.mapFleetList(result: Result<List<T>>) {
    value = if (result is Result.Success) result.data else emptyList()
  }

  fun setGestureDetection(userGesture: Boolean) {
    this.interceptByUserGesture = userGesture
  }

  fun getReverseGeoCoordinate(latLng: LatLngTruckMeImpl) {
    if (!interceptByUserGesture) {
      return
    }

    if (_latLng.value != latLng) {
      _latLng.value = latLng
      executeReverseGeocodeSearch()
    }
  }

  private fun executeReverseGeocodeSearch() {
    // cancel any active inflight search queries
    searchJob?.cancelIfActive()

    if (_latLng.value == null) {
      return
    }

    searchJob = viewModelScope.launch {
      _latLng.collect { latLng ->
        latLng ?: return@collect
        retrieveReverseGeocode(latLng)
      }
    }
  }

  // Chain of responsibilities pattern
  // [ReverseGeoCoordinate] map to [PlacesDetails] ->
  // filter coordinates within the view bounds of circle radius and lat. lng.
  // Emit the filtered coordinates and pin to the map.
  private suspend fun retrieveReverseGeocode(latLng: LatLngTruckMeImpl) {
    reverseGeoCoordinateUseCase(latLng).collect geo@{ geoResult ->
      _isReverseSearchLoading.value = geoResult == Result.Loading

      if (geoResult is Result.Success) {
        val results = geoResult.data.results?.get(0)
        geoResult.data.results?.isEmpty() ?: return@geo
        val place = Places(results?.placeId ?: "", results?.formattedAddress, null)
        addSelectedDestination(place, false)
      }

      if (geoResult is Result.Error) {
        setGestureDetection(false)
      }
    }
  }

  fun onMapReady() {
    _isMapReady.value = true
  }

  fun onMapDestroy() {
    _isMapReady.value = false
  }

  fun addSelectedDestination(
    places: Places,
    animate: Boolean = true
  ) {
    getPlaceDetails(places.placeId, animate)
    _destinationAddress.value = places
    _isDestinationAdded.value = true
    isCleared = false
  }

  private fun getPlaceDetails(
    placeId: String,
    animate: Boolean
  ) {
    viewModelScope.launch {
      placeDetailsUseCase(placeId).collect { result ->
        Timber.d(result.toString())
        if (result is Result.Success) {
          filterCoordinates(result, animate)
        }
      }
    }
  }

  private fun filterCoordinates(
    result: Result.Success<PlaceDetailsResponse>,
    animate: Boolean
  ) {
    result.data.result?.geometry?.location.let {
      _placeCoordinates.value = AnimatedLatLng(animate, LatLng(it?.lat ?: 0.0, it?.lng ?: 0.0))
      _showMarker.value = true
    }
  }

  fun clear() {
    _isDestinationAdded.value = false
    _destinationAddress.value = null
    isCleared = true
  }

  data class AnimatedLatLng(val shouldAnimate: Boolean, val latLng: LatLng)

  fun deliveryTitle(title: String) {
    _title.value = title
  }

  fun destinationAddress(address: String) {
    _address.value = address
  }

  fun selectedDriver(driver: DriverData) {
    _driver.value = driver
    Timber.e(driver.toString())
  }

  fun selectedVehicle(vehicle: VehicleData) {
    _vehicle.value = vehicle
    Timber.e(vehicle.toString())
  }

  fun freightItem(itemName: String) {
    _freightItem.value = itemName
  }

  fun deliveryMode(inbound: Boolean) {
    _boundDelivery.value = inbound
  }

  fun contact(contact: String) {
    _contact.value = contact
  }
}