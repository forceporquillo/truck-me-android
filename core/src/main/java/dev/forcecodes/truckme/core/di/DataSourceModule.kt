package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.auth.AuthStateDataSource
import dev.forcecodes.truckme.core.data.auth.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.cloud.CloudStorageDataSource
import dev.forcecodes.truckme.core.data.cloud.CloudStorageDataSourceImpl
import dev.forcecodes.truckme.core.data.driver.AddedDriverDataSourceImpl
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSourceImpl
import dev.forcecodes.truckme.core.domain.directions.DirectionsRepository
import dev.forcecodes.truckme.core.domain.directions.DirectionsRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

  @Binds
  internal abstract fun providesAuthDataSource(
    firebaseAuthStateDataSource: FirebaseAuthStateDataSource
  ): AuthStateDataSource

  @Binds
  internal abstract fun providesCloudStorageDataSource(
    cloudStorageDataSourceImpl: CloudStorageDataSourceImpl
  ): CloudStorageDataSource

  @Binds
  internal abstract fun providesRegisteredUserDataSource(
    registeredUserDataSourceImpl: RegisteredDriverDataSourceImpl
  ): RegisteredDriverDataSource

  @Binds
  internal abstract fun providesDriverDataSource(
    driverDataSourceImpl: AddedDriverDataSourceImpl
  ): DriverDataSource

  @Binds
  internal abstract fun providesDirectionsRepository(
    directionsRepositoryImpl: DirectionsRepositoryImpl
  ): DirectionsRepository
}