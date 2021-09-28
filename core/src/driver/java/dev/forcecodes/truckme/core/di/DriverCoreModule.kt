package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.AssignedDataSource
import dev.forcecodes.truckme.core.data.AssignedDeliveryDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class DriverCoreModule {

  @Binds
  internal abstract fun providesAssignedDataSource(
    assignedDeliveryDataSource: AssignedDeliveryDataSource
  ): AssignedDataSource
}