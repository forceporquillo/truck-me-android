package dev.forcecodes.truckme.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.fleets.NoActiveJobsException
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

interface AssignedDataSource {
  fun getAssignedDeliveries(id: String): Flow<Result<List<DeliveryInfo>>>
}

class AssignedDeliveryDataSource @Inject constructor(
  private val firestore: FirebaseFirestore
): AssignedDataSource {

  override fun getAssignedDeliveries(id: String): Flow<Result<List<DeliveryInfo>>> {
    return callbackFlow {
     val listenerRegistration = firestore.collection("deliveries")
        .addSnapshotListener { value, error ->
          if (value?.isEmpty == false) {
            val fleetList = mutableListOf<DeliveryInfo>()
            value.forEach { snapshot ->
              if (!value.isEmpty) {
                val data = snapshot.toObject<DeliveryInfo>()
                if (data.driverData!!.id == id) {
                  fleetList.add(data)
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
}

data class ActiveJobItems(
  val id: String,
  val title: String,
  val destination: String,
  val items: String
)