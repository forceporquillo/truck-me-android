package dev.forcecodes.truckme.core.domain.notification

import dev.forcecodes.truckme.core.db.Notification
import dev.forcecodes.truckme.core.db.NotificationDatabase
import java.util.concurrent.Executors
import javax.inject.Inject

class NotificationManager @Inject constructor() {

  private val executorService = Executors.newSingleThreadExecutor()

  fun setNotification(notification: Notification) {
    executorService.execute {
      NotificationDatabase.getInstance()
        .notificationDao()
        .setNotification(notification)
    }
  }
}