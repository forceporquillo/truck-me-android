package dev.forcecodes.truckme.core.domain.push

import dev.forcecodes.truckme.core.data.push.PushDeliveryNotificationApi
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.fcm.MessageData
import dev.forcecodes.truckme.core.fcm.PushMessage
import dev.forcecodes.truckme.core.util.FcmMessageService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManager @Inject constructor(
  @FcmMessageService private val pushDeliveryNotificationApi: PushDeliveryNotificationApi,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

  suspend fun notifyAdmin(id: String, messageData: MessageData) {
    withContext(ioDispatcher) {
      pushDeliveryNotificationApi.sendMessage(PushMessage(id, messageData))
    }
  }
}