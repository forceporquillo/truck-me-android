package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.fleets.FleetCloudStorageDataSourceImpl
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetStorageDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetsDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminDataSourceModule {

  @Binds
  internal abstract fun providesFleetsDataSourceImpl(
    fleetsDataSourceImpl: FleetsDataSourceImpl
  ): FleetDataSource

  @Binds
  internal abstract fun providesFleetCloudDataSourceImpl(
    fleetsDataSourceImpl: FleetCloudStorageDataSourceImpl
  ): FleetStorageDataSource
}