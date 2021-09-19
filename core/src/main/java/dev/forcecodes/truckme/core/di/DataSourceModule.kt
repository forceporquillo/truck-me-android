package dev.forcecodes.truckme.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.AuthStateDataSource
import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.cloud.CloudStorageDataSource
import dev.forcecodes.truckme.core.data.cloud.CloudStorageDataSourceImpl

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
}