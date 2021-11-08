package dev.forcecodes.truckme

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.forcecodes.truckme.core.db.NotificationDatabase
import timber.log.Timber

@HiltAndroidApp
class TruckMeApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    NotificationDatabase.createInstance(this)

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}