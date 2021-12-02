package dev.forcecodes.truckme.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {
  private const val DATE_FORMAT = "MM/dd/yyyy"
  private const val DEFAULT_DATE_FORMAT = "MMMM dd, yyyy"

  fun convertToTime(timeStampMillis: Long?): String? {
    return convertDate("hh:mm a", timeStampMillis)
  }

  fun formatToDate(timestampMillis: Long? = Calendar.getInstance().timeInMillis): String {
    return convertToDate(DATE_FORMAT, timestampMillis).orEmpty()
  }

  fun convertToDate(format: String = DEFAULT_DATE_FORMAT, timestampMillis: Long?): String? {
    return convertDate(format, timestampMillis)
  }

  fun convertDate(format: String, timeInMillis: Long?): String? {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis ?: 0L
    return sdf.format(calendar.time)
  }

  @JvmName("convertToTimeKt")
  fun Long?.convertToTime(): String? {
    return convertToTime(this)
  }
}