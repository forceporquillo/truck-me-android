package dev.forcecodes.truckme.core.domain.statistics

import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemMetadata
import dev.forcecodes.truckme.core.db.ItemDeliveredDtoMapper
import dev.forcecodes.truckme.core.db.ItemDeliveredEntity
import dev.forcecodes.truckme.core.db.StatisticsDao
import dev.forcecodes.truckme.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
  private val statisticsDao: StatisticsDao,
  private val deliveredItemDataSource: DeliveredItemDataSource,
  private val itemDeliveredDtoMapper: ItemDeliveredDtoMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StatisticsRepository {

  override fun getAllDeliveredItems(adminId: String): Flow<List<ItemDeliveredEntity>> {
    return statisticsDao.getAllStatsCache().flatMapLatest { items ->
      if (items.isEmpty()) {
        deliveredItemDataSource.getAllDeliveredItems(adminId).map { list ->
          list.mapAndCache()
        }
      } else {
        flowOf(items)
      }
    }
      .distinctUntilChanged()
  }

  override fun forceRefresh(adminId: String): Flow<Boolean> {
    return flow {
      emit(false)
      val getDeliveredItems = deliveredItemDataSource.getAllDeliveredItems(adminId)
        .map { list ->
          list.mapAndCache()
          true
        }

      emitAll(getDeliveredItems)
    }.flowOn(ioDispatcher)
  }

  override fun getCacheItems(): Flow<List<ItemDeliveredEntity>> {
    return statisticsDao.getAllStatsCache()
  }

  private suspend fun List<DeliveredItemMetadata>.mapAndCache(): List<ItemDeliveredEntity> {
    return map { metadata ->
      itemDeliveredDtoMapper(metadata)
    }.also(::cacheItems)
  }

  private fun cacheItems(entity: List<ItemDeliveredEntity>) =
    entity.forEach(statisticsDao::saveStatistics)
}

interface StatisticsRepository {
  fun getAllDeliveredItems(adminId: String): Flow<List<ItemDeliveredEntity>>
  fun getCacheItems(): Flow<List<ItemDeliveredEntity>>
  fun forceRefresh(adminId: String): Flow<Boolean>
}
