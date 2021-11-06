package dev.forcecodes.truckme.core.data.delivery

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.ItemDelivered
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface DeliveredItemDataSource {
  fun getAllDeliveredItems(adminId: String): Flow<List<ItemDelivered>>
  fun getDailyStats(adminId: String): Flow<List<ItemDelivered>>
}

class DeliveredItemDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : DeliveredItemDataSource {

  override fun getAllDeliveredItems(adminId: String): Flow<List<ItemDelivered>> {
    return callbackFlow {
      val listenerRegistration = firestore.collection("delivered")
        .addSnapshotListener { value, _ ->
          val list = mutableListOf<ItemDelivered>()
          if (value?.isEmpty == false) {
            value.forEach {
              val itemDelivered = it.toObject<ItemDelivered>()
              if (itemDelivered.deliveryInfo?.assignedAdminId == adminId) {
                list.add(itemDelivered)
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

  override fun getDailyStats(adminId: String): Flow<List<ItemDelivered>> {
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
class DeliveredItemMapper @Inject constructor() : DomainMapperSingle<ItemDelivered, DeliveredItem> {

  override suspend fun invoke(from: ItemDelivered): DeliveredItem {
    return from.run {
      DeliveredItem(
        id = deliveryInfo?.id,
        title = deliveryInfo?.title,
        address = deliveryInfo?.destination?.address ?: "",
        items = deliveryInfo?.items,
        date = convertToDate(timestamp.toLong()),
        time = convertToTime(timestamp.toLong())
      )
    }
  }

  private fun convertToTime(timeStampMillis: Long): String? {
    return convertDate("hh:mm a", timeStampMillis)
  }

  private fun convertToDate(timeStampMillis: Long): String? {
    return convertDate("MMMM dd, yyyy", timeStampMillis)
  }

  private fun convertDate(format: String, timeInMillis: Long): String? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(timeInMillis))
  }
}