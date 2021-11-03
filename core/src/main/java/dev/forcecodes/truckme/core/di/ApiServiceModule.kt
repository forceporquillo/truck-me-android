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
import dev.forcecodes.truckme.core.util.InternalApi
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
  @Singleton
  @Provides
  internal fun providesRetrofit(
    @InternalApi gson: Gson,
    @InternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit {
    return Retrofit.Builder().apply {
      baseUrl(BuildConfig.BASE_URL)
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