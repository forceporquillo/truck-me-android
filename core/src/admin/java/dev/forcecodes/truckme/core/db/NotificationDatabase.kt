package dev.forcecodes.truckme.core.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    Notification::class
  ],
  version = 2, exportSchema = false
)
abstract class NotificationDatabase : RoomDatabase() {
  abstract fun notificationDao(): NotificationDao

  companion object {
    private var INSTANCE: NotificationDatabase? = null

    @JvmStatic
    fun createInstance(context: Context): NotificationDatabase {
      synchronized(NotificationDatabase::class) {
        if (INSTANCE == null) {
          val database = Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            "notification_database.db"
          )
            .fallbackToDestructiveMigration()
            .build()

          INSTANCE = database
        }
      }
      return INSTANCE!!
    }

    @JvmStatic
    fun getInstance(): NotificationDatabase {
      return INSTANCE!!
    }
  }
}

@Dao
interface NotificationDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun setNotification(conversations: Notification)

  @Query("SELECT * FROM notification WHERE id=:id")
  fun getNotification(id: String): Notification?
}

@Entity
data class Notification(
  @PrimaryKey
  val id: String,
  val isNotified: Boolean = false
)