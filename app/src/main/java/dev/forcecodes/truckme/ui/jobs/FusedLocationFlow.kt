@file:SuppressLint("MissingPermission")
@file:Suppress("Unused")

package dev.forcecodes.truckme.ui.jobs

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import timber.log.Timber.Forest

fun createLocationRequest(): LocationRequest =
  LocationRequest.create().apply {
    interval = 3000
    fastestInterval = 1000
    expirationTime = 28800000
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
  }

@ExperimentalCoroutinesApi
@Throws(SecurityException::class)
inline fun fusedLocationFlow(
  context: Context,
  configLocationRequest: LocationRequest.() -> Unit
): Flow<Location> = fusedLocationFlow(
  context = context,
  locationRequest = LocationRequest.create().apply(configLocationRequest)
)

@Throws(SecurityException::class)
@ExperimentalCoroutinesApi
fun fusedLocationFlow(
  context: Context,
  locationRequest: LocationRequest = createLocationRequest()
): Flow<Location> = channelFlow {
  val locationClient = LocationServices.getFusedLocationProviderClient(context)
  val locationCallback = object : LocationCallback() {
    override fun onLocationResult(result: LocationResult) {
      result.locations.forEachByIndex {
        Timber.d("locations:forEachByIndex $it")
        offerCatching(it)
      }
      Timber.d("onLocationResult $result")
     //
    }
  }
  locationClient.lastLocation.await<Location?>()?.let { send(it) }
  locationClient.requestLocationUpdates(locationRequest, locationCallback)
  awaitClose { locationClient.removeLocationUpdates(locationCallback) }

}

private suspend fun FusedLocationProviderClient.requestLocationUpdates(
  locationRequest: LocationRequest,
  locationCallback: LocationCallback
) = requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()).await()

/**
 * [SendChannel.offer] that returns `false` when this [SendChannel.isClosedForSend], instead of
 * throwing.
 *
 * [SendChannel.offer] throws when the channel is closed. In race conditions, especially when using
 * multithreaded dispatchers, that can lead to uncaught exceptions as offer is often called from
 * non suspending functions that don't catch the default [CancellationException] or any other
 * exception that might be the cause of the closing of the channel.
 */
// Copy pasted from splitties.coroutines
fun <E> SendChannel<E>.offerCatching(element: E): Boolean {
  return runCatching { tryOffer(element) }.getOrDefault(false)
}

/**
 * Iterates the receiver [List] using an index instead of an [Iterator] like [forEach] would do.
 * Using this function saves an [Iterator] allocation, which is good for immutable lists or usages
 * confined to a single thread like UI thread only use.
 * However, this method will not detect concurrent modification, except if the size of the list
 * changes on an iteration as a result, which may lead to unpredictable behavior.
 *
 * @param action the action to invoke on each list element.
 */
// Copy pasted from splitties.collections
inline fun <T> List<T>.forEachByIndex(action: (T) -> Unit) {
  val initialSize = size
  for (i in 0..lastIndex) {
    if (size != initialSize) throw ConcurrentModificationException()
    action(get(i))
  }
}