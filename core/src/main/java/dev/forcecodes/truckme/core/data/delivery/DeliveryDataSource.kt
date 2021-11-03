package dev.forcecodes.truckme.core.data.delivery

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.flow.Flow

interface DeliveryDataSource {
  suspend fun addDelivery(deliveryInfo: DeliveryInfo): Task<DocumentReference>
  fun getActiveJobById(jobId: String): Flow<Result<DeliveryInfo>>
}