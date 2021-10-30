package dev.forcecodes.truckme.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.data.places.PlacesApiService
import dev.forcecodes.truckme.core.util.PlacesBackendApi
import dev.forcecodes.truckme.core.util.InternalApi
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesApiServiceModule {

  @PlacesBackendApi
  @Singleton
  @Provides
  internal fun providesPlacesApiService(
    @InternalApi retrofit: Retrofit
  ): PlacesApiService = retrofit.create()
}