package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.AssignedDataSource
import dev.forcecodes.truckme.core.data.AssignedDeliveryDataSource
import dev.forcecodes.truckme.core.data.UpdateDeliveryDataSource
import dev.forcecodes.truckme.core.data.UpdateDeliveryDataSourceImpl
import dev.forcecodes.truckme.core.data.UpdateMyFleetState
import dev.forcecodes.truckme.core.data.admin.AdminDataSource
import dev.forcecodes.truckme.core.data.admin.AdminDataSourceImpl
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.domain.directions.DirectionsRepository
import dev.forcecodes.truckme.core.domain.directions.DirectionsRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DriverCoreModule {

  @Binds
  internal abstract fun providesAssignedDataSource(
    assignedDeliveryDataSource: AssignedDeliveryDataSource
  ): AssignedDataSource

  @Binds
  internal abstract fun providesDirectionsRepository(
    directionsRepositoryImpl: DirectionsRepositoryImpl
  ): DirectionsRepository

  @Binds
  internal abstract fun providesAdminDataSource(
    adminDataSourceImpl: AdminDataSourceImpl
  ): AdminDataSource

  @Binds
  internal abstract fun providesUpdateMyFleetState(
    adminDataSourceImpl: UpdateMyFleetState
  ): FleetDataSource

  @Binds
  internal abstract fun providesUpdateDeliveryDataSource(
    updateDeliveryDataSourceImpl: UpdateDeliveryDataSourceImpl
  ): UpdateDeliveryDataSource
}