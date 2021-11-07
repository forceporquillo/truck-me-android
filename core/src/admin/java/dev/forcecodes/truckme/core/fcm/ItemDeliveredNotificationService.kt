package dev.forcecodes.truckme.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.karn.notify.Notify
import timber.log.Timber

class ItemDeliveredNotificationService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Timber.e("New firebase token: $token")
    // do nothing here
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    Notify
      .with(applicationContext)
      .asBigText { // this: Payload.Content.Default
        title = "New item has been delivered."
        bigText = "Delivered: ${remoteMessage.data["title"] as String} " +
          "with ${remoteMessage.data["items"]} item was delivered " +
          "by ${remoteMessage.data["driver"]}."
        text =
          "Delivered: ${remoteMessage.data["title"] as String} " +
            "with ${remoteMessage.data["items"]}"
      }
      .show()

    Timber.e("Message received ${remoteMessage.senderId} content: ${remoteMessage.data["content"]}")
  }
}