package dev.forcecodes.truckme.core.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemMetadata
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Database(
  entities = [
    ItemDeliveredEntity::class
  ],
  version = 1, exportSchema = false
)

abstract class StatisticsCacheDatabase : RoomDatabase() {
  abstract fun statisticsDao(): StatisticsDao
}

@Dao
interface StatisticsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveStatistics(stats: ItemDeliveredEntity)

  @Query("SELECT * FROM ItemDeliveredEntity")
  fun getAllStatsCache(): Flow<List<ItemDeliveredEntity>>
}

class ItemDeliveredDtoMapper @Inject constructor() :
  DomainMapperSingle<DeliveredItemMetadata, ItemDeliveredEntity> {

  override suspend fun invoke(from: DeliveredItemMetadata): ItemDeliveredEntity {
    return from.run {
      val deliveryInfo = from.deliveryInfo

      ItemDeliveredEntity(
        documentId = from.documentId,
        items = deliveryInfo.items,
        startTimestamp = deliveryInfo.startTimestamp,
        completedTimestamp = deliveryInfo.completedTimestamp,
        estimatedTimeDuration = deliveryInfo.estimatedTimeDuration,
        driverName = deliveryInfo.driverData?.driverName,
        bound = deliveryInfo.inbound
          ?: throw IllegalStateException("Delivery bound state cannot be null.")
      )
    }
  }
}