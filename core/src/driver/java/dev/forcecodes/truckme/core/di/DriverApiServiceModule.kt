package dev.forcecodes.truckme.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.api.DirectionsApiService
import dev.forcecodes.truckme.core.data.push.PushDeliveryNotificationApi
import dev.forcecodes.truckme.core.util.DirectionsBackendApi
import dev.forcecodes.truckme.core.util.FcmBackendApi
import dev.forcecodes.truckme.core.util.FcmMessageService
import dev.forcecodes.truckme.core.util.InternalApi
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriverApiServiceModule {

  @FcmMessageService
  @Singleton
  @Provides
  internal fun providesFcmApiService(
    @FcmBackendApi retrofit: Retrofit
  ): PushDeliveryNotificationApi = retrofit.create()
}