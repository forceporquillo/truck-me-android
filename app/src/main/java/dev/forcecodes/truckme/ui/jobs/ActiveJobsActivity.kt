package dev.forcecodes.truckme.ui.jobs

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.binding.viewBinding
import dev.forcecodes.truckme.databinding.ActivityActiveJobsBinding
import dev.forcecodes.truckme.extensions.applyTranslucentStatusBar
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.util.PermissionUtils

@AndroidEntryPoint
class ActiveJobsActivity : AppCompatActivity(), OnMapReadyCallback {

  private val binding by viewBinding(ActivityActiveJobsBinding::inflate)

  private var fusedLocationClient: FusedLocationProviderClient? = null
  private lateinit var map: GoogleMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    initToolbar()

    fusedLocationClient = LocationServices
      .getFusedLocationProviderClient(this)

    supportMapFragment().getMapAsync(this)
    setOnClickListeners()
  }

  private fun setOnClickListeners() {
    binding.emergencyButton.setOnClickListener { goToDialPhone() }
  }

  private fun goToDialPhone() {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:0123456789")
    startActivity(intent)
  }

  private fun initToolbar() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    binding.materialToolbar.applyTranslucentStatusBar()
    fillDecor(binding.materialToolbar)
  }

  override fun onMapReady(googleMap: GoogleMap?) {
    map = googleMap ?: return
    enableMyLocation()
    with(map) {
      setMapStyle(
        MapStyleOptions.loadRawResourceStyle(
          this@ActiveJobsActivity,
          R.raw.map_stype_standard
        )
      )
      setMinZoomPreference(15f)
      setMaxZoomPreference(18f)
      this
    }.uiSettings.apply {
      isMyLocationButtonEnabled = false
      isRotateGesturesEnabled = false
      isMapToolbarEnabled = true
    }
  }

  @SuppressLint("MissingPermission")
  private fun enableMyLocation() {
    if (!::map.isInitialized) return
    PermissionUtils.checkSelfPermission(
      this,
      permission.ACCESS_FINE_LOCATION
    ) {
      // access granted
      map.isMyLocationEnabled = true
      fusedLocationClient?.lastLocation
        ?.addOnSuccessListener { location ->
          animateCameraToMyLocation(location)
        }
    }
  }

  private fun animateCameraToMyLocation(location: Location) {
    val latLng = LatLng(location.latitude, location.longitude)
    val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
    map.animateCamera(cameraUpdate)
  }

  private fun supportMapFragment() =
    supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
}