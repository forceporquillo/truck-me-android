package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSourceImpl
import dev.forcecodes.truckme.core.data.delivery.DeliveryDataSourceImpl
import dev.forcecodes.truckme.core.data.fleets.FleetCloudStorageDataSourceImpl
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetStorageDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetsDataSourceImpl
import dev.forcecodes.truckme.core.data.fleets.FreeUpFleetState
import dev.forcecodes.truckme.core.data.fleets.FreeUpFleetStateImpl
import dev.forcecodes.truckme.core.domain.places.PlacesRepository
import dev.forcecodes.truckme.core.domain.places.PlacesRepositoryImpl

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

  @Binds
  internal abstract fun providesPlacesRepositoryImpl(
    placesRepositoryImpl: PlacesRepositoryImpl
  ): PlacesRepository

  @Binds
  internal abstract fun providesAddDeliveryDataSourceImpl(
    deliveryDataSourceImpl: DeliveryDataSourceImpl
  ): AdminDeliveryDataSource

  @Binds
  internal abstract fun deliveredItemDataSource(
    deliveredItemDataSourceImpl: DeliveredItemDataSourceImpl
  ): DeliveredItemDataSource

  @Binds
  internal abstract fun providesFreeUpFleetState(
    freeUpFleetStateImpl: FreeUpFleetStateImpl
  ): FreeUpFleetState
}