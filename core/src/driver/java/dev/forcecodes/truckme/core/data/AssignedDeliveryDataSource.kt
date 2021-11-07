package dev.forcecodes.truckme.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.delivery.NoActiveJobsException
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.ItemDelivered
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject

interface AssignedDataSource {
  fun getAssignedDeliveries(id: String): Flow<Result<List<DeliveryInfoMetaData>>>
  fun getJobById(jobId: String) : Flow<Result<DeliveryInfoMetaData>>
  fun confirmDelivery(deliveryInfo: DeliveryInfo, jobId: String)
}

data class DeliveryInfoMetaData(
  val documentId: String? = null,
  val deliveryInfo: DeliveryInfo? = null
)

class AssignedDeliveryDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
): AssignedDataSource {

  override fun getAssignedDeliveries(id: String): Flow<Result<List<DeliveryInfoMetaData>>> {
    return callbackFlow {
     val listenerRegistration = firestore.collection("deliveries")
        .addSnapshotListener { value, error ->
          if (value?.isEmpty == false) {
            val fleetList = mutableListOf<DeliveryInfoMetaData>()
            value.forEach { snapshot ->
              if (!value.isEmpty) {
                val data = snapshot.toObject<DeliveryInfo>()
                if (data.driverData!!.id == id && !data.completed) {
                  fleetList.add(DeliveryInfoMetaData(snapshot.id, data))
                }
              }
            }
            tryOffer(Result.Success(fleetList))
          } else {
            tryOffer(Result.Error(NoActiveJobsException(error)))
          }
        }
      awaitClose { listenerRegistration.remove() }
    }
      .distinctUntilChanged()
  }

  override fun getJobById(jobId: String): Flow<Result<DeliveryInfoMetaData>> {
    return callbackFlow {
      val listenerRegistration = firestore.collection("deliveries")
        .addSnapshotListener { value, error ->
          if (value?.isEmpty == false) {
            for (snapshot in value) {
              if (value.isEmpty) {
                return@addSnapshotListener
              }
              val data = snapshot.toObject<DeliveryInfo>()
              if (data.id == jobId) {
                tryOffer(Result.Success(DeliveryInfoMetaData(snapshot.id, data)))
                break
              }
            }
          } else {
            tryOffer(Result.Error(NoActiveJobsException(error)))
          }
          Unit
        }
      awaitClose { listenerRegistration.remove() }
    }
      .distinctUntilChanged()
  }

  override fun confirmDelivery(deliveryInfo: DeliveryInfo, jobId: String) {
    update(jobId)
    firestore.collection("delivered")
      .add(ItemDelivered(deliveryInfo))
  }

  private fun update(jobId: String) {
    firestore.collection("deliveries")
      .document(jobId)
      .update(mapOf("completed" to true))
  }
}

data class ActiveJobItems(
  val id: String,
  val title: String,
  val destination: String,
  val items: String
)