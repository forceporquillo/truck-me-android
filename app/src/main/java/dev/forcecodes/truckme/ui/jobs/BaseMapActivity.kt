@file:Suppress("unused")

package dev.forcecodes.truckme.ui.jobs

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ktx.model.circleOptions
import com.google.maps.android.ktx.model.markerOptions
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.raw
import dev.forcecodes.truckme.binding.viewBinding
import dev.forcecodes.truckme.databinding.ActivityActiveJobsBinding
import dev.forcecodes.truckme.extensions.applyTranslucentStatusBar
import dev.forcecodes.truckme.extensions.doOnApplyWindowInsets
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.util.MapUtils
import timber.log.Timber

abstract class BaseMapActivity : AppCompatActivity(), OnMapReadyCallback {

  protected val binding by viewBinding(ActivityActiveJobsBinding::inflate)
  private lateinit var googleMap: GoogleMap

  private var movingCabMarker: Marker? = null
  private var previousLatLng: LatLng? = null
  private var currentLatLng: LatLng? = null

  protected var polyline: Polyline? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    setToolbarInset()
    setNavBarInset()

    supportMapFragment().getMapAsync(this)
  }

  protected fun addCarMarker(latLng: LatLng): Marker? {
    val carBitmapDescriptor = MapUtils.getCarBitmap(this)

    val markerOptions = markerOptions {
      position(latLng)
      flat(true)
      icon(carBitmapDescriptor)
    }

    return googleMap.addMarker(markerOptions)
  }


  protected fun dropOffDestinationMarker(latLng: LatLng?): Marker? {
    if (latLng == null) {
      return null
    }
    val endMarkerBitmapDescriptor = MapUtils.getEndMarkerBitmap(this)

    val markerOptions = markerOptions {
      icon(endMarkerBitmapDescriptor)
      position(latLng)
    }

    endDestinationMarkerPoint(latLng)

    return googleMap.addMarker(markerOptions)
  }

  private fun endDestinationMarkerPoint(latLng: LatLng) {
    val endPointMarker = MapUtils.getEndDestinationMarker()

    val endMarkerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(endPointMarker)

    val markerOptions = markerOptions {
      position(latLng)
      flat(true)
      icon(endMarkerBitmapDescriptor)
    }

    val circleOptions = circleOptions {
      center(latLng)
      radius(10.0)
      strokeWidth(0f)
      fillColor(Color.parseColor("#25B4D1"))
    }

    googleMap.addCircle(circleOptions)
    googleMap.addMarker(markerOptions)
  }

  protected fun showPolylinePath(
    polylineOptions: PolylineOptions,
    line: String
  ): Polyline {

    val polyLine = PolyUtil.decode(line)

    val latLngBounds = LatLngBounds.Builder()

    polyLine.forEach { latLng ->
      polylineOptions.add(latLng)
      latLngBounds.include(latLng)
    }

    val bounds = latLngBounds.build()

    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))

    polyline = googleMap.addPolyline(polylineOptions)
    return polyline as Polyline
  }

  protected fun startDestination(latLng: LatLng) {
    Timber.e("Start destination $latLng")
    moveCamera(latLng)
    animateCamera(latLng)
  }

  protected fun moveCamera(latLng: LatLng) {
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
  }

  private fun animateCamera(latLng: LatLng) {
    val cameraPosition = CameraPosition.Builder().target(latLng).zoom(17.5f).build()
    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
  }

  open fun onAttachIntentDialPadListener(dialNumber: String?) {
    if (dialNumber == null) {
      return
    }
    binding.emergencyButton.setOnClickListener { onStartDialPad(dialNumber) }
  }

  open fun onAttachIntentMessageListener(phoneNumber: String?) {
    if (phoneNumber == null) {
      return
    }
    binding.chatButton.setOnClickListener { onStartMessage(phoneNumber) }
  }

  private fun onStartMessage(dialNumber: String) {
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("sms:$dialNumber")
    startActivity(smsIntent)
  }

  private fun onStartDialPad(dialNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$dialNumber")
    startActivity(intent)
  }

  override fun onMapReady(googleMap: GoogleMap?) {
    this.googleMap = googleMap ?: return
    onMapIsReady(googleMap)
    with(googleMap) {
      reduceLabelMapStyle()
      minMaxZoomPrefs()
      uiSettingsPrefs()
    }
  }

  @SuppressLint("MissingPermission")
  abstract fun onMapIsReady(googleMap: GoogleMap)

  private fun GoogleMap.minMaxZoomPrefs() {
    setMinZoomPreference(15f)
    setMaxZoomPreference(18f)
  }

  private fun GoogleMap.uiSettingsPrefs() {
    uiSettings.apply {
      isMyLocationButtonEnabled = false
      isRotateGesturesEnabled = false
      isMapToolbarEnabled = true
    }
  }

  private fun GoogleMap.reduceLabelMapStyle() {
    val mapStyleOptions = loadRawResourceStyle(this@BaseMapActivity, raw.map_stype_standard)
    setMapStyle(mapStyleOptions)
  }

  private fun setToolbarInset() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    binding.materialToolbar.applyTranslucentStatusBar()
    fillDecor(binding.materialToolbar, true)
  }

  private fun setNavBarInset() {
    binding.buttonsParent.doOnApplyWindowInsets { view, windowInsetsCompat, viewPaddingState ->
      val navBar = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars())
      view.updatePadding(bottom = viewPaddingState.bottom + navBar.bottom)
    }
  }

  private fun supportMapFragment() =
    supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

  protected fun updateCarLocation(latLng: LatLng) {
    Timber.d("updateCarLocation $latLng")

    if (movingCabMarker == null) {
      Timber.d("Car Start Location $latLng")
  //    Toast.makeText(applicationContext, "Car Start Location $latLng", Toast.LENGTH_SHORT).show()
      movingCabMarker = addCarMarker(latLng)
    }
    if (previousLatLng == null) {
      currentLatLng = latLng
      previousLatLng = currentLatLng
      movingCabMarker?.position = currentLatLng
      movingCabMarker?.setAnchor(0.5f, 0.5f)
      //  animateCamera(currentLatLng!!)
    } else {
      previousLatLng = currentLatLng
      currentLatLng = latLng
      val valueAnimator = MapUtils.carAnimator()
      valueAnimator.addUpdateListener { va ->
        if (currentLatLng != null && previousLatLng != null) {
          val multiplier = va.animatedFraction
          val nextLocation = LatLng(
            multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
            multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
          )
          Timber.d("Car Next Location $nextLocation")
     //     Toast.makeText(applicationContext, "Car Next Location $nextLocation", Toast.LENGTH_SHORT).show()
          movingCabMarker?.position = nextLocation

          val rotation = SphericalUtil.computeHeading(previousLatLng!!, currentLatLng)

          if (!rotation.isNaN()) {
            movingCabMarker?.rotation = rotation.toFloat()
          }
          movingCabMarker?.setAnchor(0.5f, 0.5f)
          // animateCamera(nextLocation)
        }
      }
      valueAnimator.start()
    }
  }

  fun animateMarker(
    marker: Marker,
    toPosition: LatLng,
    hideMarker: Boolean
  ) {

    if (movingCabMarker == null) {
      movingCabMarker = addCarMarker(toPosition)
    }

    val handler = Handler(Looper.myLooper()!!)

    val start = SystemClock.uptimeMillis()

    val proj: Projection = googleMap.projection
    val startPoint: Point = proj.toScreenLocation(marker.position)
    val startLatLng: LatLng = proj.fromScreenLocation(startPoint)
    val duration: Long = 2000

    val interpolator: Interpolator = LinearInterpolator()

    handler.post(object : Runnable {
      override fun run() {
        val elapsed = SystemClock.uptimeMillis() - start
        val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)

        val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
        val lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude

        marker.position = LatLng(lat, lng)
        if (t < 1.0) {
          // Post again 16ms later.
          handler.postDelayed(this, 16)
        } else {
          marker.isVisible = !hideMarker
        }
      }
    })
  }

  fun changePositionSmoothly(
    marker: MarkerOptions?,
    newLatLng: LatLng
  ) {

    if (marker == null) {
      return
    }

    val animation = ValueAnimator.ofFloat(0f, 100f)
    var previousStep = 0f
    val deltaLatitude = newLatLng.latitude - marker.position.latitude
    val deltaLongitude = newLatLng.longitude - marker.position.longitude

    animation.duration = 1500

    animation.addUpdateListener { updatedAnimation ->
      val deltaStep = updatedAnimation.animatedValue as Float - previousStep
      previousStep = updatedAnimation.animatedValue as Float
      marker.position(
        LatLng(
          marker.position.latitude + deltaLatitude * deltaStep * 1 / 100,
          marker.position.longitude + deltaStep * deltaLongitude * 1 / 100
        )
      )
    }
    animation.start()
  }

  fun calculateBearing(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double
  ): Float {
    val sourceLatLng = LatLng(lat1, lng1)
    val destinationLatLng = LatLng(lat2, lng2)
    return SphericalUtil.computeHeading(sourceLatLng, destinationLatLng).toFloat()
  }
}