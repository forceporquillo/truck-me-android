package dev.forcecodes.truckme.ui.dashboard

import android.Manifest.permission
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.model.DriverData
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.core.model.Places
import dev.forcecodes.truckme.core.model.VehicleData
import dev.forcecodes.truckme.databinding.BottomSheetDeliveryInfoBinding
import dev.forcecodes.truckme.databinding.FragmentMapDeliveryBinding
import dev.forcecodes.truckme.extensions.attachProgressToMain
import dev.forcecodes.truckme.extensions.navigateOnButtonClick
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.postRunnable
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.slideDown
import dev.forcecodes.truckme.extensions.slideUp
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.extensions.withToolbarElevationListener
import dev.forcecodes.truckme.ui.dashboard.MapDeliverySharedViewModel.AnimatedLatLng
import dev.forcecodes.truckme.ui.dashboard.MapDeliverySharedViewModel.SubmitUiEvent
import dev.forcecodes.truckme.util.PermissionUtils
import dev.forcecodes.truckme.util.PermissionUtils.PermissionDeniedDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapDeliveryFragment : BaseMapFragment(R.layout.fragment_map_delivery),
  OnRequestPermissionsResultCallback, OnCameraMoveStartedListener,
  OnCameraIdleListener {

  private val binding by viewBinding(FragmentMapDeliveryBinding::bind)
  private val sharedViewModel by mapNavGraphViewModels()

  private var _bottomSheet: BottomSheetDeliveryInfoBinding? = null
  private val bottomSheet get() = _bottomSheet!!

  private val mapStyleOptions by lazy {
    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_stype_standard)
  }

  private var fusedLocationClient: FusedLocationProviderClient? = null
  private var permissionDenied = false
  private lateinit var map: GoogleMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    _bottomSheet = binding.bottomSheetParent
    super.onViewCreated(view, savedInstanceState)

    initLifecycleOwners()
    initBottomSheet()
    observeDataChanges()

    binding.searchDestination.navigateOnButtonClick(R.id.to_search)
    binding.bottomSheetParent.submit.setOnClickListener { sharedViewModel.submit() }
    binding.bottomSheetParent.titleEt.textChangeObserver { sharedViewModel.deliveryTitle(it) }
  }

  private fun initLifecycleOwners() {
    binding.apply {
      viewModel = sharedViewModel
      lifecycleOwner = viewLifecycleOwner
      bottomSheetParent.viewModel = sharedViewModel
      bottomSheetParent.lifecycleOwner = viewLifecycleOwner
    }
  }

  override fun onMapReady(googleMap: GoogleMap?) {
    map = googleMap ?: return
    formatMapStyle()
    enableMyLocation()
    setMapListener()
    observeOnLifecycleStarted(500L) {
      binding.progressContainer.isVisible = false
    }
    sharedViewModel.onMapReady()
  }

  private fun setMapListener() {
    map.setOnCameraMoveStartedListener(this)
    map.setOnCameraIdleListener(this)
  }

  private fun formatMapStyle() {
    with(map) {
      setMapStyle(mapStyleOptions)
      setMinZoomPreference(MIN_ZOOM)
      setMaxZoomPreference(MAX_ZOOM)
      this
    }.uiSettings.apply {
      isMyLocationButtonEnabled = false
      isRotateGesturesEnabled = false
      isMapToolbarEnabled = true
    }
  }

  @SuppressLint("MissingPermission")
  private fun enableMyLocation() {
    if (!::map.isInitialized) return
    PermissionUtils.checkSelfPermission(
      requireContext(),
      permission.ACCESS_FINE_LOCATION
    ) {
      // access granted
      map.isMyLocationEnabled = true
      if (!sharedViewModel.isLocationSet) {
        fusedLocationClient?.lastLocation
          ?.addOnSuccessListener { location ->
            animateCameraToMyLocation(location)
          }
        sharedViewModel.isLocationSet = true
      }
    }
  }

  private fun animateCameraToMyLocation(location: Location?) {
    val latLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
    val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
    map.animateCamera(cameraUpdate)
  }

  private fun observeDataChanges() = repeatOnLifecycleParallel {
    launch { sharedViewModel.isMapReady.collect(::emitWhenMapReady) }
    launch { sharedViewModel.driverList.collect(::setDriverDropDown) }
    launch { sharedViewModel.vehicleList.collect(::setVehicleDropDown) }
    launch { sharedViewModel.destinationAddress.collect(::setDestinationAddress) }
    launch { sharedViewModel.isDestinationAdded.collect(::hideOrCollapseSheet) }
    launch { sharedViewModel.submitDeliveryUiEvent.collect(::handleResult) }
  }

  private fun handleResult(submitUiEvent: SubmitUiEvent) {
    attachProgressToMain(submitUiEvent.isLoading)
    if (submitUiEvent.isSuccess) {
      // todo add animation
      navigateUp()
    }
  }

  private fun setVehicleDropDown(vehicleUris: List<VehicleUri>) {
    val availableVehicle = vehicleUris.filter {
      !it.hasOngoingDeliveries
    }.filter {
      it.isActive
    }.map { it.name }
    with(bottomSheet.vehicleEt) {
      if (availableVehicle.isEmpty()) {
        bottomSheet.vehicleTextLayout.hint = context.getString(string.no_available_vehicles)
      } else {
        bottomSheet.vehicleTextLayout.hint = context.getString(string.vehicles)
      }

      setSelected(vehicleUris) { vehicleUri, name ->
        if (vehicleUri.name == name) {
          sharedViewModel.selectedVehicle(VehicleData(vehicleUri.id, vehicleUri.name))
          true
        } else false
      }
      this
    }.setTextAdapter(availableVehicle)
  }

  private fun setDriverDropDown(driversUri: List<DriverUri>) {
    val availableDrivers = driversUri.filter {
      !it.hasOngoingDeliveries
    }.filter {
      it.isActive
    }.map { it.fullName }
    with(bottomSheet.driverEt) {
      if (availableDrivers.isEmpty()) {
        bottomSheet.driverTextLayout.hint = context.getString(string.no_available_drivers)
      } else {
        bottomSheet.driverTextLayout.hint = context.getString(string.driver)
      }

      setSelected(driversUri) { driver, fullName ->
        if (driver.fullName == fullName) {
          // automatically populates the contacts information
          // when a specific item is selected.
          setContactDetails(driver)
          true
        } else false
      }
      this
    }.setTextAdapter(availableDrivers)
  }

  private fun <T : FleetUiModel> MaterialAutoCompleteTextView.setSelected(
    data: List<T>,
    condition: (items: T, text: String) -> Boolean
  ) {
    textChangeObserver {
      for (items in data) {
        if (condition(items, it)) {
          break
        }
      }
    }
  }

  private fun AutoCompleteTextView.setTextAdapter(list: List<String?>) {
    if (adapter != null && adapter.count > 0) {
      clearListSelection()
    }
    val adapter = ArrayAdapter(context, R.layout.delivery_delegate_list, list)
    setAdapter(adapter)
  }

  private fun setContactDetails(driver: DriverUri) {
    bottomSheet.phoneNumberEt.setText(driver.contact)

    val driverData = DriverData(driver.id, driver.fullName, driver.profile ?: "")
    sharedViewModel.selectedDriver(driverData)
  }

  private fun setDestinationAddress(places: Places?) {
    binding.apply {
      address.isVisible = !places?.address.isNullOrEmpty()
      bottomSheetParent.root.isVisible = places != null
      if (places != null) {
        destination.text = places.title
        address.text = places.address
        bottomSheetParent.destinationEt.setText(places.title ?: places.address)
        return@apply
      }
      destination.text = getString(R.string.select_destinations)
    }
  }

  private fun emitWhenMapReady(isReady: Boolean) {
    if (!isReady) {
      return
    }
    observeOnLifecycleStarted {
      sharedViewModel.placeCoordinates.collect(::animateCamera)
    }
  }

  private fun animateCamera(animatedLatLng: AnimatedLatLng?) {
    animatedLatLng ?: return

    if (!::map.isInitialized) {
      return
    }

    val (shouldAnimate, latLng) = animatedLatLng

    if (!shouldAnimate) {
      return
    }

    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAX_ZOOM))
  }

  override fun onCameraMoveStarted(p0: Int) {
    if (isDestinationAdded()) {
      return
    }

    if (sharedViewModel.isDestinationAdded.value) {
      hideOrCollapseSheet(false)
    }

    sharedViewModel.setGestureDetection(p0 == REASON_GESTURE)
  }

  override fun onCameraIdle() {
    if (isDestinationAdded()) {
      return
    }

    hideOrCollapseSheet(true)

    val latLatLng = map.projection.visibleRegion.latLngBounds.center

    val latLngModel =
      LatLngTruckMeImpl(latLatLng.latitude, latLatLng.longitude)

    sharedViewModel.getReverseGeoCoordinate(latLngModel)
  }

  private fun initBottomSheet() {
    postRunnable {
      val bottomSheetParent = bottomSheet
      bottomSheetParent.apply {
        val behavior = BottomSheetBehavior.from(root)
        nestedScroll.withToolbarElevationListener(toolbarFrame)
        behavior.peekHeight = toolbar.layoutParams.height
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        root.slideDown()
      }
    }
  }

  private fun isDestinationAdded() =
    sharedViewModel.destinationAddress.value == null

  override fun onDestroyView() {
    super.onDestroyView()
    _bottomSheet = null
    sharedViewModel.onMapDestroy()
  }

  override fun onResume() {
    super.onResume()
    if (permissionDenied) {
      showMissingPermissionError()
    }
  }

  private fun showMissingPermissionError() {
    PermissionDeniedDialog.newInstance(false)
      .show(parentFragmentManager, "dialog")
  }

  @Suppress("DEPRECATION")
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (requestCode != PermissionUtils.LOCATION_PERMISSION) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
      return
    }
    if (!PermissionUtils.isPermissionGranted(
        permissions,
        grantResults,
        permission.ACCESS_FINE_LOCATION
      )
    ) {
      permissionDenied = true
    } else {
      enableMyLocation()
    }
  }

  private fun hideOrCollapseSheet(slideUp: Boolean) {
    if (sharedViewModel.isCleared) {
      return
    }

    if (slideUp) {
      bottomSheet.root.slideUp()
      return
    }

    bottomSheet.root.slideDown()
  }

  companion object {
    private const val MAX_ZOOM = 18f
    private const val MIN_ZOOM = 15f
    private const val REASON_GESTURE = 1
  }
}