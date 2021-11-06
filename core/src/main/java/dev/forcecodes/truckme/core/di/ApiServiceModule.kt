package dev.forcecodes.truckme.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.util.DEFAULT_TIMEOUT
import dev.forcecodes.truckme.core.util.FcmBackendApi
import dev.forcecodes.truckme.core.util.FcmMessageService
import dev.forcecodes.truckme.core.util.InternalApi
import dev.forcecodes.truckme.core.util.PlacesBackendApi
import dev.forcecodes.truckme.core.util.checkMainThread
import dev.forcecodes.truckme.core.util.delegatingCallFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

  @InternalApi
  @Singleton
  @Provides
  internal fun providesGson(): Gson {
    return GsonBuilder().apply {
      serializeNulls()
      setLenient()
    }.create()
  }

  @InternalApi
  @Provides
  internal fun providesRetrofitForPlaces(
    @InternalApi gson: Gson,
    @InternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit = createRetrofitInstance(gson, okHttpClient, BuildConfig.BASE_URL)

  @FcmBackendApi
  @Provides
  internal fun providesRetrofitForFcm(
    @InternalApi gson: Gson,
    @InternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit = createRetrofitInstance(gson, okHttpClient, BuildConfig.BASE_URL_FCM)

  private fun createRetrofitInstance(
    gson: Gson,
    okHttpClient: Lazy<OkHttpClient>,
    baseUrl: String
  ): Retrofit {
    return Retrofit.Builder().apply {
      baseUrl(baseUrl)
      addConverterFactory(GsonConverterFactory.create(gson))
      delegatingCallFactory(okHttpClient)
    }.build()
  }

  @InternalApi
  @Provides
  internal fun providesOkHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
  }

  @InternalApi
  @Singleton
  @Provides
  internal fun providesOkHttpClient(
    @InternalApi interceptor: HttpLoggingInterceptor
  ): OkHttpClient {
    return checkMainThread {
      Builder().apply {
        connectTimeout(DEFAULT_TIMEOUT, SECONDS)
        readTimeout(DEFAULT_TIMEOUT, SECONDS)
        writeTimeout(DEFAULT_TIMEOUT, SECONDS)

        if (BuildConfig.DEBUG) {
          addInterceptor(interceptor)
        }

        addInterceptor(Interceptor { chain ->
          val request = chain.request().newBuilder()
            .addHeader("Accept-Encoding", "identity")
            .build()

          chain.proceed(request)
        })
      }.build()
    }
  }
}