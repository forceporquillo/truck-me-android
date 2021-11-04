package dev.forcecodes.truckme.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.forcecodes.truckme.core.db.Notification
import dev.forcecodes.truckme.core.db.NotificationDatabase
import io.karn.notify.Notify
import timber.log.Timber
import java.util.concurrent.Executors

class ItemDeliveredNotificationService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Timber.e("New firebase token: $token")
    // do nothing here
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val notification = getNotification(remoteMessage.data["jobId"] as String)

    if (notification != null && !notification.isNotified) {
      Notify
        .with(applicationContext)
        .asBigText { // this: Payload.Content.Default
          title = "New item has been delivered."
          bigText  = "Delivered: ${remoteMessage.data["title"] as String} " +
            "with ${remoteMessage.data["items"]} item was delivered " +
            "by ${remoteMessage.data["driver"]}."
          text =
            "Delivered: ${remoteMessage.data["title"] as String} " +
              "with ${remoteMessage.data["items"]}"
        }
        .show()
      setOffNotificationFor(notification.id)
    }

    Timber.e("Message received ${remoteMessage.senderId} content: ${remoteMessage.data["content"]}")
  }

  private fun getNotification(jobId: String): Notification? {
    return NotificationDatabase.getInstance()
      .notificationDao().getNotification(jobId)
  }

  private fun setOffNotificationFor(jobId: String) {
    Executors.newSingleThreadExecutor().submit {
      NotificationDatabase.getInstance()
        .notificationDao()
        .setNotification(Notification(jobId, true))
    }
  }
}