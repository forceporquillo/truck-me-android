package dev.forcecodes.truckme.base

import androidx.lifecycle.ViewModel
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.Channel

interface UiActionEvent

abstract class BaseViewModel<E : UiActionEvent> : ViewModel() {

  protected val mUiEvents = Channel<E>(capacity = Channel.CONFLATED)

  @Suppress("UNCHECKED_CAST")
  fun sendUiEvent(event: UiActionEvent) {
    mUiEvents.tryOffer(event as E)
  }
}
