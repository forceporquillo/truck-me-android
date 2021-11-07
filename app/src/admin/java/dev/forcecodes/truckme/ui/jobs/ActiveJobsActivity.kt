package dev.forcecodes.truckme.ui.jobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.LatLngData
import dev.forcecodes.truckme.databinding.BottomSheetDeliveryStatusBinding
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.onLifecycleStarted
import dev.forcecodes.truckme.extensions.postKt
import dev.forcecodes.truckme.extensions.px
import dev.forcecodes.truckme.extensions.updateIconTextDrawable
import dev.forcecodes.truckme.util.DirectionUiModel
import dev.forcecodes.truckme.util.MapUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class ActiveJobsActivity : BaseMapActivity() {

  private val viewModel by viewModels<ActiveJobsViewModel>()

  private var _deliveryStatusBinding: BottomSheetDeliveryStatusBinding? = null
  private val deliveryStatusBinding get() = _deliveryStatusBinding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.getJob(intent.extras?.getString("job_item_id")!!)

    initBottomSheet()

    _deliveryStatusBinding = binding.bottomSheetParent
    deliveryStatusBinding.startDeliveryButton.isGone = true

    binding.notifyOrConfirmButton.updateIconTextDrawable(
      R.drawable.ic_notify,
      string.active_jobs_notify
    ).setOnClickListener {
      notifyDelivery()
    }
    hideLoadingState()
  }

  private fun initBottomSheet() {
    binding.coordinator.postKt {
      val bottomSheetParent = deliveryStatusBinding
      bottomSheetParent.run {
        val behavior = BottomSheetBehavior.from(root)
        behavior.peekHeight = 24.px

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = false
      }
    }
  }

  override fun onMapIsReady(googleMap: GoogleMap) {
    onLifecycleStarted {
      viewModel.deliveryInfo.collect { value: DeliveryInfo? ->
        value ?: return@collect

        if (value.completed) {
          exitWhenCompleted()
        }

        val deliveryState = if (value.inbound == true) {
          getString(string.inbound_delivery_message)
        } else {
          getString(string.outbound_delivery_message)
        }

        onAttachIntentDialPadListener(value.contactNumber)
        onAttachIntentMessageListener(value.contactNumber)

        binding.inboundDelivery.text = deliveryState

        deliveryStatusBinding.deliverTitle.text = value.title
        deliveryStatusBinding.etaTime.text = value.eta
        binding.distance.text = value.distanceRemApprox
        binding.deliverTitle.text = value.title
        binding.destination.text = value.destination?.address
        deliveryStatusBinding.duration.text = value.duration

        bindImageWith(binding.driverImage, value.driverData?.profileUrl)

        try {
          val (lat, lng) = value.currentCoordinates ?: LatLngData(0.0, 0.0)
          val latLng = LatLng(lat!!, lng!!)
          updateCarLocation(latLng, true)
        } catch (e: NullPointerException) {
          Timber.e(e)
        }
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
      }
    }
  }

  private fun exitWhenCompleted() {
    onLifecycleStarted {
      delay(1000L)
      Toast.makeText(applicationContext, "Item delivery complete.", Toast.LENGTH_SHORT).show()
      finish()
    }
  }

  private fun notifyDelivery() {
    // val title = viewModel.deliveryTitle
    // val arrivalTime = deliveryStatusBinding.etaTime.text.toString()

    MaterialAlertDialogBuilder(this)
      .setTitle("Enable Notification for this Item")
      .setMessage("You are about to receive notifications when this item has arrived.")
      .setNegativeButton("Cancel") { _, _ ->
        // Respond to neutral button press
      }
      .setPositiveButton("Notify") { _, _ ->
        viewModel.notifyWhenDelivered()
      }
      .show()
  }

  override fun onDestroy() {
    super.onDestroy()
    _deliveryStatusBinding = null
  }
}