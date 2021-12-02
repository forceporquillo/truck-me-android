package dev.forcecodes.truckme.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.db.StatisticsCacheDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun providesStatisticsDatabase(
    @ApplicationContext context: Context
  ): StatisticsCacheDatabase {
    return Room.inMemoryDatabaseBuilder(
      context, StatisticsCacheDatabase::class.java
    ).build()
  }

  @Provides
  @Singleton
  fun providesStatisticsDao(
    statisticsCacheDatabase: StatisticsCacheDatabase
  ) = statisticsCacheDatabase.statisticsDao()
}