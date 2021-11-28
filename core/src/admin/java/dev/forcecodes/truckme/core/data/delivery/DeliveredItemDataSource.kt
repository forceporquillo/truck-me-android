package dev.forcecodes.truckme.core.data.delivery

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.model.ItemDelivered
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DeliveredItemMetadata(
  val documentId: String,
  val deliveryInfo: DeliveryInfo
  )

interface DeliveredItemDataSource {
  fun getAllDeliveredItems(adminId: String): Flow<List<DeliveredItemMetadata>>
  fun getDailyStats(adminId: String): Flow<List<DeliveredItemMetadata>>
}

class DeliveredItemDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : DeliveredItemDataSource {

  override fun getAllDeliveredItems(adminId: String): Flow<List<DeliveredItemMetadata>> {
    return callbackFlow<List<DeliveredItemMetadata>> {
      val listenerRegistration = firestore.collection("deliveries")
        .addSnapshotListener { value, _ ->
          val deliveredItems = mutableListOf<DeliveredItemMetadata>()
          if (value?.isEmpty == false) {
            for (snapshot in value) {
              val deliveryInfo = snapshot.toObject<DeliveryInfo>()
              val documentId = snapshot.id

              if (deliveryInfo.isAssignedToAdmin(adminId)) {
                val deliveredMetadata = DeliveredItemMetadata(documentId, deliveryInfo)
                deliveredItems.add(deliveredMetadata)
              }
            }
            tryOffer(deliveredItems)
          } else {
            tryOffer(emptyList())
          }
        }
      awaitClose { listenerRegistration.remove() }
    }
      .distinctUntilChanged()
  }

  private fun DeliveryInfo.isAssignedToAdmin(adminId: String): Boolean {
    return this.assignedAdminId == adminId && this.completed
  }

  override fun getDailyStats(adminId: String): Flow<List<DeliveredItemMetadata>> {
    return getAllDeliveredItems(adminId)
  }
}

class HistoryUseCase @Inject constructor(
  private val deliveredItemDataSource: DeliveredItemDataSource,
  private val deliveredItemMapper: DeliveredItemMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, List<DeliveredItem>>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<List<DeliveredItem>>> {
    return deliveredItemDataSource.getAllDeliveredItems(parameters).map { list ->
      val mappedList = list
        .sortedByDescending { deliveryInfo ->
          deliveryInfo.deliveryInfo.completedTimestamp
        }
        .map { itemDelivered ->
          deliveredItemMapper(itemDelivered.deliveryInfo)
        }
      Result.Success(mappedList)
    }
  }
}

data class ItemDeliveredStats(
  val documentId: String?,
  val itemTitle: String?,
  val timeStarted: String?,
  val timeCompleted: String?,
  val estimatedTimeArrival: String?,
  val dateAccomplish: String?,
  val deliveryStatus: String?,
  val metadata: ItemMetaData
)

data class ItemMetaData(
  val completedTimestamp: Long?,
  val driverName: String?,
  val bound: Boolean,
  val date: String?
)

data class DeliveredItem(
  val id: String?,
  val title: String?,
  val address: String?,
  val items: String?,
  val date: String?,
  val time: String?
)

@Singleton
class DeliveredItemMapper @Inject constructor() : DomainMapperSingle<DeliveryInfo, DeliveredItem> {

  override suspend fun invoke(from: DeliveryInfo): DeliveredItem {
    return from.run {
      DeliveredItem(
        id = id,
        title = title,
        address = destination?.address ?: "",
        items = items,
        date = convertToDate(timeStampMillis = completedTimestamp),
        time = convertToTime(completedTimestamp)
      )
    }
  }
}

fun convertToTime(timeStampMillis: Long?): String? {
  return convertDate("hh:mm a", timeStampMillis)
}

fun formatToDate(timeStampMillis: Long? = Calendar.getInstance().timeInMillis): String {
  return convertToDate("MM/dd/yyyy", timeStampMillis) ?: ""
}

fun convertToDate(format: String = "MMMM dd, yyyy", timeStampMillis: Long?): String? {
  return convertDate(format, timeStampMillis)
}

fun convertDate(format: String, timeInMillis: Long?): String? {
  val sdf = SimpleDateFormat(format, Locale.getDefault())
  val calendar = Calendar.getInstance()
  calendar.timeInMillis = timeInMillis ?: 0L
  return sdf.format(calendar.time)
}