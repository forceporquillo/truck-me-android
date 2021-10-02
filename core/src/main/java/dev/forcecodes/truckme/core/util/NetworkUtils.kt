package dev.forcecodes.truckme.core.util

import android.os.Looper
import dagger.Lazy
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class PlacesBackendApi

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class PlacesInternalApi

internal const val DEFAULT_TIMEOUT = 15L

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun <T> checkMainThread(block: () -> T): T =
  if (Looper.myLooper() == Looper.getMainLooper()) {
    throw IllegalStateException("Object initialized on main thread.")
  } else {
    block()
  }

@PublishedApi
internal inline fun Retrofit.Builder.callFactory(
  crossinline body: (Request) -> Call
) = callFactory { request -> body(request) }

@Suppress("NOTHING_TO_INLINE")
inline fun Retrofit.Builder.delegatingCallFactory(
  delegate: Lazy<OkHttpClient>
): Retrofit.Builder = callFactory {
  delegate.get().newCall(it)
}
