package dev.forcecodes.truckme.core.data

import com.google.firebase.firestore.FirebaseFirestore
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.ItemDelivered
import dev.forcecodes.truckme.core.model.LatLngData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface UpdateDeliveryDataSource {
  fun duration(duration: String, jobId: String)
  fun distanceRemainingApprox(distance: String, jobId: String)
  fun arrivalTime(arrival: String, jobId: String)
  fun startDestination(startDestination: LatLngData, jobId: String)
  fun distanceRemaining(distance: String, jobId: String)
  fun onStartDelivery(jobId: String)
  fun onFinishDelivery(jobId: String)
  fun updateCurrentLocation(coordinates: LatLngData, jobId: String)
  fun confirmDelivery(deliveryInfo: DeliveryInfo, jobId: String)
}

class UpdateDeliveryDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore,
  @ApplicationScope private val externalScope: CoroutineScope,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UpdateDeliveryDataSource {

  override fun startDestination(startDestination: LatLngData, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("startDestination" to startDestination))
    }
  }

  override fun updateCurrentLocation(coordinates: LatLngData, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("currentCoordinates" to coordinates))
    }
  }

  override fun confirmDelivery(deliveryInfo: DeliveryInfo, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("delivered")
        .add(ItemDelivered(deliveryInfo))
    }
  }

  override fun onFinishDelivery(jobId: String) {
    Timber.e(jobId)
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(
          mapOf(
            "active" to true,
            "completed" to true
          )
        )
    }
  }

  override fun duration(duration: String, jobId: String) {
    Timber.e(jobId)
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("duration" to duration))
    }
  }

  override fun onStartDelivery(jobId: String) {
    Timber.e(jobId)
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("active" to true, "started" to true))
    }
  }

  override fun distanceRemaining(distance: String, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("distanceRemaining" to distance))
    }
  }

  override fun arrivalTime(arrival: String, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("eta" to arrival))
    }
  }

  override fun distanceRemainingApprox(distance: String, jobId: String) {
    externalScope.launch(ioDispatcher) {
      firestore.collection("deliveries")
        .document(jobId)
        .update(mapOf("distanceRemApprox" to distance))
    }
  }
}