package dev.forcecodes.truckme.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ItemDeliveredEntity(
  @PrimaryKey
  val documentId: String,
  val items: String?,
  val startTimestamp: Long?,
  val completedTimestamp: Long?,
  val estimatedTimeDuration: Long?,
  val driverName: String?,
  val bound: Boolean,
)
