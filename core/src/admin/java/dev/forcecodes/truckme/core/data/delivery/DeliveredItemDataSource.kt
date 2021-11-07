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
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface DeliveredItemDataSource {
  fun getAllDeliveredItems(adminId: String): Flow<List<DeliveryInfo>>
  fun getDailyStats(adminId: String): Flow<List<DeliveryInfo>>
}

class DeliveredItemDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : DeliveredItemDataSource {

  override fun getAllDeliveredItems(adminId: String): Flow<List<DeliveryInfo>> {
    return callbackFlow {
      val listenerRegistration = firestore.collection("deliveries")
        .addSnapshotListener { value, _ ->
          val list = mutableListOf<DeliveryInfo>()
          if (value?.isEmpty == false) {
            value.forEach {
              val deliveryInfo = it.toObject<DeliveryInfo>()
              if (deliveryInfo.assignedAdminId == adminId && deliveryInfo.completed) {
                list.add(deliveryInfo)
              }
            }
            tryOffer(list)
          } else {
            tryOffer(emptyList())
          }
        }
      awaitClose { listenerRegistration.remove() }
    }
  }

  override fun getDailyStats(adminId: String): Flow<List<DeliveryInfo>> {
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
      val mappedList = list.map { itemDelivered -> deliveredItemMapper(itemDelivered) }
      Result.Success(mappedList)
    }
  }
}

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
        date = convertToDate(timeStampMillis = timestamp),
        time = convertToTime(timestamp)
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