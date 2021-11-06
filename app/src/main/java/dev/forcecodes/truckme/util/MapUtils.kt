package dev.forcecodes.truckme.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.os.Build.VERSION_CODES
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.ktx.model.polylineOptions
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.extensions.bitmapDescriptorFromVector
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan

object MapUtils {

  fun getCarBitmap(context: Context): BitmapDescriptor {
    return context.bitmapDescriptorFromVector(R.drawable.ic_truck)
  }

  fun getEndMarkerBitmap(context: Context): BitmapDescriptor {
    return context.bitmapDescriptorFromVector(R.drawable.ic_pin_drop_location)
  }

  @RequiresApi(VERSION_CODES.N)
  fun calculateEstimatedTime(durationInSeconds: Int): String? {
    val now = Calendar.getInstance()
    now.add(Calendar.SECOND, durationInSeconds)

    return SimpleDateFormat("hh:mm a", Locale.getDefault())
      .format(now.timeInMillis)
  }

  @RequiresApi(VERSION_CODES.N)
  fun roundDistance(value: Double): String {
    val df = DecimalFormat("#.#")
    return df.format(value / 1000)
  }

  fun createTealPath(context: Context): PolylineOptions {
    val pathColor = ContextCompat.getColor(context, R.color.teal_700)
    return polylineOptions {
      color(pathColor)
      width(8f)
      startCap(RoundCap())
      endCap(RoundCap())
      jointType(JointType.ROUND)
    }
  }

  fun carAnimator(): ValueAnimator {
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.duration = 3000
    valueAnimator.interpolator = LinearInterpolator()
    return valueAnimator
  }

  fun getEndDestinationMarker(): Bitmap {
    val height = 20
    val width = 20
    val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = Color.parseColor("#25B4D1")
    paint.style = Paint.Style.FILL
    paint.isAntiAlias = true
    canvas.drawCircle(width.toFloat() / 2, height.toFloat() / 2, 360f, paint)
    return bitmap
  }

  fun getRotation(start: LatLng, end: LatLng): Float {
    val latDifference: Double = abs(start.latitude - end.latitude)
    val lngDifference: Double = abs(start.longitude - end.longitude)
    var rotation = -1F
    when {
      start.latitude < end.latitude && start.longitude < end.longitude -> {
        rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
      }
      start.latitude >= end.latitude && start.longitude < end.longitude -> {
        rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
      }
      start.latitude >= end.latitude && start.longitude >= end.longitude -> {
        rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
      }
      start.latitude < end.latitude && start.longitude >= end.longitude -> {
        rotation =
          (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
      }
    }
    return rotation
  }
}