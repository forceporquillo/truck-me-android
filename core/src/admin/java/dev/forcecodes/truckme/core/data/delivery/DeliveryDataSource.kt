package dev.forcecodes.truckme.core.data.delivery

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder.IN_PROGRESS
import dev.forcecodes.truckme.core.domain.dashboard.GetOrder
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject
import javax.inject.Singleton

interface AdminDeliveryDataSource : DeliveryDataSource {
  fun getActiveJobsByOrder(
    getOrder: GetOrder
  ): Flow<Result<List<DeliveryInfo>>>

  suspend fun deleteJobById(jobId: String)
}

@Singleton
class DeliveryDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore,
  @ApplicationScope private val externalScope: CoroutineScope
) : AdminDeliveryDataSource {

  private companion object {
    private const val DELIVERY = "deliveries"
  }

  override suspend fun deleteJobById(jobId: String) {
    queryActiveJobs { querySnapshot ->
      for (snapshot in querySnapshot) {
        val deliveryInfo = snapshot.toObject<DeliveryInfo>()
        if (jobId == deliveryInfo.id) {
          deleteJob(snapshot.id)
          break
        }
      }
    }.stateIn(externalScope)
      .collect()
  }

  override fun getActiveJobsByOrder(getOrder: GetOrder) = queryActiveJobs { snapshot ->
    val fleetList = mutableListOf<DeliveryInfo>()
    val data = snapshot.toObjects<DeliveryInfo>()

    val (adminId, order) = getOrder

    data.forEach { info ->
      if (adminId == info.assignedAdminId && !info.completed) {
        fleetList.add(info)
      }
    }
    fleetList.filter { deliveryInfo ->
      if (order == IN_PROGRESS) {
        deliveryInfo.active
      } else {
        !deliveryInfo.active
      }
    }
  }

  override fun getActiveJobById(jobId: String): Flow<Result<DeliveryInfo>> {
    return queryActiveJobs {
      var deliveryInfo: DeliveryInfo? = null
      val data = it.toObjects<DeliveryInfo>()
      for (info in data) {
        if (jobId == info.id) {
          deliveryInfo = info
          break
        }
      }
      deliveryInfo!!
    }
  }

  private fun <T> queryActiveJobs(block: (QuerySnapshot) -> T) = callbackFlow {
    val listenerRegistration = firestore.collection(DELIVERY)
      .addSnapshotListener { value, error ->
        if (value?.isEmpty == false) {
          tryOffer(Result.Success(block(value)))
        } else {
          tryOffer(Result.Error(NoActiveJobsException(error)))
        }
        Unit
      }
    awaitClose { listenerRegistration.remove() }
  }
    .distinctUntilChanged()

  override suspend fun addDelivery(
    deliveryInfo: DeliveryInfo
  ): Task<DocumentReference> {
    return firestore.collection(DELIVERY)
      .add(deliveryInfo)
  }

  private fun deleteJob(jobId: String) {
    firestore.collection(DELIVERY)
      .document(jobId)
      .delete()
  }
}
