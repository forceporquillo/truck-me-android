package dev.forcecodes.truckme.core.data.dashboard

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.delivery.DeliveryDataSource
import dev.forcecodes.truckme.core.data.fleets.NoActiveJobsException
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.WhileViewSubscribed
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore,
  @ApplicationScope private val externalScope: CoroutineScope
) : DeliveryDataSource {

  private companion object {
    private const val DELIVERY = "deliveries"
  }

  private val activeJobs = callbackFlow {
    val listenerRegistration = firestore.collection(DELIVERY)
      .addSnapshotListener { value, error ->
        if (value?.isEmpty == false) {
          val fleetList = mutableListOf<DeliveryInfo>()
          value.forEach {
            if (!value.isEmpty) {
              val data = it.toObject<DeliveryInfo>()
              fleetList.add(data)
            }
          }
          tryOffer(Result.Success(fleetList))
        } else {
          tryOffer(Result.Error(NoActiveJobsException(error)))
        }
        Unit
      }
    awaitClose { listenerRegistration.remove() }
  }.shareIn(externalScope, WhileViewSubscribed)

  override fun getActiveJobs() = activeJobs

  override suspend fun addDelivery(
    deliveryInfo: DeliveryInfo
  ): Task<DocumentReference> {
    return firestore.collection(DELIVERY)
      .add(deliveryInfo)
  }
}
