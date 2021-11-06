package dev.forcecodes.truckme.ui.jobs

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.LatLngData
import dev.forcecodes.truckme.databinding.BottomSheetDeliveryStatusBinding
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.updateIconTextDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest

@AndroidEntryPoint
class ActiveJobsActivity : BaseMapActivity() {

  private val viewModel by viewModels<ActiveJobsViewModel>()

  private var _deliveryStatusBinding: BottomSheetDeliveryStatusBinding? = null
  private val deliveryStatusBinding get() = _deliveryStatusBinding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.getJob(intent.extras?.getString("job_item_id")!!)

    _deliveryStatusBinding = binding.bottomSheetParent
    deliveryStatusBinding.startDeliveryButton.isGone = true

    binding.notifyOrConfirmButton.updateIconTextDrawable(
      R.drawable.ic_notify,
      string.active_jobs_notify
    ).setOnClickListener {
        notifyDelivery()
    }
  }

  override fun onMapIsReady(googleMap: GoogleMap) {
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(STARTED) {
        viewModel.deliveryInfo.collect { value: DeliveryInfo? ->
          value ?: return@collect
          val latLngData = value.coordinates?.finalDestination
          moveCamera(googleMap, latLngData)

          val deliveryState = if (value.inbound == true) {
            getString(string.inbound_delivery_message)
          } else {
            getString(string.outbound_delivery_message)
          }

          onAttachIntentDialPadListener(value.contactNumber)
          onAttachIntentMessageListener(value.contactNumber)

          binding.inboundDelivery.text = deliveryState

          deliveryStatusBinding.deliverTitle.text = value.title
          binding.deliverTitle.text = value.title
          binding.destination.text = value.destination?.address

          bindImageWith(binding.driverImage, value.driverData?.profileUrl)
        }
      }
    }
  }

  private fun moveCamera(googleMap: GoogleMap, latLngData: LatLngData?) {
    val latLng = LatLng(latLngData?.lat!!, latLngData.lng!!)
    val cameraUpdateFactory = CameraUpdateFactory.newLatLng(latLng)
    googleMap.animateCamera(cameraUpdateFactory)
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