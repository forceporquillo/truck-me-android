package dev.forcecodes.truckme.core.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel

fun <E> SendChannel<E>.tryOffer(element: E): Boolean = try {
    trySend(element).isSuccess
} catch (t: Throwable) {
    false // Ignore
}

/**
 * Cancel the Job if it's active.
 */
fun Job?.cancelIfActive() {
    if (this?.isActive == true) {
        cancel()
    }
}
