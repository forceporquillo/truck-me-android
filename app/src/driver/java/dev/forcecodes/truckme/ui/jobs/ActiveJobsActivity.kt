package dev.forcecodes.truckme.ui.jobs

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.data.DeliveryInfoMetaData
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.LatLngData
import dev.forcecodes.truckme.core.model.LatLngTruckMeImpl
import dev.forcecodes.truckme.databinding.BottomSheetDeliveryStatusBinding
import dev.forcecodes.truckme.databinding.TimeArrivalBottomSheetBinding
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.onLifecycleStarted
import dev.forcecodes.truckme.extensions.postKt
import dev.forcecodes.truckme.extensions.px
import dev.forcecodes.truckme.extensions.slideDownHide
import dev.forcecodes.truckme.extensions.slideTop
import dev.forcecodes.truckme.extensions.toast
import dev.forcecodes.truckme.extensions.updateIconTextDrawable
import dev.forcecodes.truckme.util.DirectionUiModel
import dev.forcecodes.truckme.util.MapUtils
import dev.forcecodes.truckme.util.distanceLeft
import dev.forcecodes.truckme.util.getTimeTaken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import timber.log.Timber

@AndroidEntryPoint
class ActiveJobsActivity : BaseMapActivity() {

  private val viewModel by viewModels<ActiveJobsViewModel>()

  private var _deliveryStatusBinding: BottomSheetDeliveryStatusBinding? = null
  private val deliveryStatusBinding get() = _deliveryStatusBinding!!

  private var _arriveInBottomSheet: TimeArrivalBottomSheetBinding? = null
  private val arriveInBottomSheet get() = _arriveInBottomSheet!!

  private lateinit var fusedLocationProvider: FusedLocationProviderClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loadDestinationPath()

    fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

    _deliveryStatusBinding = binding.bottomSheetParent
    _arriveInBottomSheet = binding.arrivalTimeBottomSheet

    initBottomSheet()

    binding.notifyOrConfirmButton.updateIconTextDrawable(
      R.drawable.ic_confirm_check,
      string.active_jobs_confirm
    )

    binding.notifyButton.setOnClickListener { confirmDelivery() }

    hideLoadingState()
  }

  private fun loadDestinationPath() {
    val jobId = intent.extras?.getString("job_item_id")
    viewModel.getJob(jobId!!)
  }

  @SuppressLint("NewApi")
  override fun onStart() {
    super.onStart()

    onLifecycleStarted {
      fusedLocationFlow(this@ActiveJobsActivity).collect { location: Location ->
        try {
          val latLng = LatLng(location.latitude, location.longitude)
          updateCarLocation(latLng)

          val latLngTruckMeImpl = LatLngTruckMeImpl(location.latitude, location.longitude)
          viewModel.updateCurrentLocation(latLngTruckMeImpl)
        } catch (e: Exception) {
          Timber.e(e)
        }
      }
    }

    onLifecycleStarted {
      try {
        viewModel.currentLatLng.flatMapMerge { currentLatLng ->
          viewModel.endDirection.map { endLatLng ->
            if (endLatLng == null || currentLatLng == null) {
              return@map 0.0
            }
            SphericalUtil.computeDistanceBetween(currentLatLng, endLatLng)
          }
        }.collect { distance ->
          Timber.d("Distance Remaining: $distance")

          val timeTaken = getTimeTaken(distance)
          arriveInBottomSheet.arrivalTime.text = timeTaken
          viewModel.estimatedArrivalTime = timeTaken

          val distanceLeft = distanceLeft(distance)
          arriveInBottomSheet.distanceLeft.text = distanceLeft
          viewModel.distanceRemaining = distanceLeft
        }
      } catch (e: Exception) {
        Timber.e(e)
      }
    }

    onLifecycleStarted {
      viewModel.directions.collect { value: DirectionUiModel? ->
        if (value == null) {
          return@collect
        }

        value.polyline?.let { line ->
          val pathOptions = MapUtils.createTealPath(this@ActiveJobsActivity)
          showPolylinePath(pathOptions, line)
        }

        value.endLocation?.let { latLng ->
          dropOffDestinationMarker(latLng)
        }

        val durationLess = "< ${value.duration}"

        val estimatedArrival = MapUtils.calculateEstimatedTime(value.durationInSeconds)
        with(deliveryStatusBinding) {
          duration.text = durationLess
          etaTime.text = estimatedArrival
        }

        binding.distance.text = value.distance
        viewModel.durationLess = durationLess
        viewModel.estimatedArrivalTime = estimatedArrival
      }
    }

    onLifecycleStarted {
      viewModel.adminPhone.collect { contactNumber ->
        onAttachIntentDialPadListener(contactNumber)
        onAttachIntentMessageListener(contactNumber)
      }
    }
  }

  private fun initBottomSheet() {
    var shouldClickOnce = true

    val onReloadDirections: (View) -> Unit = {
      if (shouldClickOnce) {
        binding.deliveryInfoHeaderView.slideTop()
        deliveryStatusBinding.root.slideDownHide()

        polyline?.remove()
        viewModel.startAndReload()
        shouldClickOnce = false

        showArrivalModalSheet()
      }
    }
    showCurrentDeliveryState(onReloadDirections)
  }

  private fun showCurrentDeliveryState(onReloadDirections: (View) -> Unit) {
    onLifecycleStarted {
      viewModel.jobData.map { deliveryMetadata ->
        deliveryMetadata?.deliveryInfo?.started == true
      }.collect { hasStarted ->
        if (!hasStarted) {
          showDeliveryDetailsModalSheet(onReloadDirections, false)
          return@collect
        } else {
          showArrivalModalSheet()
          showDeliveryDetailsModalSheet(onReloadDirections, true)
        }
      }
    }
  }

  private fun showDeliveryDetailsModalSheet(onReloadDirections: (View) -> Unit, collapse: Boolean) {
    binding.coordinator.postKt {
      val bottomSheetParent = deliveryStatusBinding
      bottomSheetParent.run {
        root.isGone = collapse

        val behavior = BottomSheetBehavior.from(root)
        behavior.peekHeight = 24.px

        val stateCollapse = if (collapse)  {
          BottomSheetBehavior.STATE_COLLAPSED
        } else {
          BottomSheetBehavior.STATE_EXPANDED
        }

        behavior.state = stateCollapse
        behavior.isHideable = false

        startDeliveryButton.setOnClickListener(onReloadDirections)
      }
    }
  }

  private fun showArrivalModalSheet() {
    with(binding.arrivalTimeBottomSheet) {
      root.postDelayed({
        root.isVisible = true

        val arrivalBottomSheet = arriveInBottomSheet

        binding.coordinator.postKt {
          arrivalBottomSheet.apply {
            val behavior = BottomSheetBehavior.from(root)
            behavior.peekHeight = 24.px
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isHideable = false
          }
        }
      }, 500L)
    }
  }

  override fun onAttachIntentDialPadListener(dialNumber: String?) {
    viewModel.adminPhone.value?.let { phoneNumber ->
      Timber.e(phoneNumber)
      super.onAttachIntentDialPadListener(phoneNumber)
    }
  }

  override fun onAttachIntentMessageListener(phoneNumber: String?) {
    viewModel.adminPhone.value?.let { _phoneNumber ->
      Timber.e(phoneNumber)
      super.onAttachIntentMessageListener(_phoneNumber)
    }
  }

  @SuppressLint("MissingPermission")
  override fun onMapIsReady(googleMap: GoogleMap) {
    onLifecycleStarted {
      viewModel.jobData.collect { value: DeliveryInfoMetaData? ->
        if (value == null) {
          return@collect
        }

        val deliveryInfo = value.deliveryInfo

        val deliveryState = if (deliveryInfo?.inbound == true) {
          getString(string.in_bound_delivery)
        } else {
          getString(string.out_bound_delivery)
        }

        binding.inboundDelivery.text = deliveryState
        deliveryStatusBinding.deliverTitle.text = deliveryInfo?.title
        binding.deliverTitle.text = deliveryInfo?.title
        binding.destination.text = deliveryInfo?.destination?.address

        bindImageWith(binding.driverImage, deliveryInfo?.driverData?.profileUrl)

        fusedLocationProvider.lastLocation
          .addOnSuccessListener { location ->
            getDirections(location, deliveryInfo)
          }
          .addOnFailureListener {
            Toast.makeText(
              applicationContext,
              "Error: ${it.message}",
              Toast.LENGTH_SHORT
            ).show()
          }
      }
    }
  }

  private fun getDirections(location: Location?, deliveryInfo: DeliveryInfo?) {
    if (location == null) {
      toast(getString(string.location_state_message))
      return
    }

    try {
      viewModel.getDirections(
        LatLngTruckMeImpl(
          location.latitude,
          location.longitude
        ), deliveryInfo?.destination!!.placeId
      )
      val latLng = LatLng(location.latitude, location.longitude)
      startDestination(latLng)

      viewModel.startDestination = LatLngData(
        location.latitude,
        location.longitude
      )

    } catch (e: NullPointerException) {
      toast(getString(string.location_state_message))
    }
  }

  private fun confirmDelivery() {
    val title = viewModel.deliveryTitle
    val arrivalTime = deliveryStatusBinding.etaTime.text.toString()

    MaterialAlertDialogBuilder(this)
      .setTitle("Confirm Item Delivered")
      .setMessage("You are about to confirm your item delivery named $title with the expected time arrival at $arrivalTime.")
      .setNegativeButton("Minimize") { _, _ ->
        // Respond to neutral button press
      }
      .setPositiveButton("Confirm") { _, _ ->
        viewModel.notifyAdmin()
        onLifecycleStarted {
          delay(500L)
          finish()
        }
      }
      .show()
  }

  override fun onDestroy() {
    super.onDestroy()
    _deliveryStatusBinding = null
    _arriveInBottomSheet = null
  }
}
