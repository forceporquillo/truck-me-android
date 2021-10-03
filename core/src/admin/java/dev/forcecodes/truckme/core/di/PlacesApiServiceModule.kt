package dev.forcecodes.truckme.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.data.places.PlacesApiService
import dev.forcecodes.truckme.core.util.DEFAULT_TIMEOUT
import dev.forcecodes.truckme.core.util.PlacesBackendApi
import dev.forcecodes.truckme.core.util.PlacesInternalApi
import dev.forcecodes.truckme.core.util.checkMainThread
import dev.forcecodes.truckme.core.util.delegatingCallFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesApiServiceModule {

  @PlacesInternalApi
  @Singleton
  @Provides
  internal fun providesGson(): Gson {
    return GsonBuilder().apply {
      serializeNulls()
      setLenient()
    }.create()
  }

  @PlacesInternalApi
  @Singleton
  @Provides
  internal fun providesRetrofit(
    @PlacesInternalApi gson: Gson,
    @PlacesInternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit {
    return Retrofit.Builder().apply {
      baseUrl("https://maps.googleapis.com")
      addConverterFactory(GsonConverterFactory.create(gson))
      delegatingCallFactory(okHttpClient)
    }.build()
  }

  @PlacesInternalApi
  @Provides
  internal fun providesOkHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
  }

  @PlacesInternalApi
  @Singleton
  @Provides
  internal fun providesOkHttpClient(
    @PlacesInternalApi interceptor: HttpLoggingInterceptor
  ): OkHttpClient {
    return checkMainThread {
      OkHttpClient.Builder().apply {
        connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)

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

  @PlacesBackendApi
  @Singleton
  @Provides
  internal fun providesPlacesApiService(
    @PlacesInternalApi retrofit: Retrofit
  ): PlacesApiService = retrofit.create()
}