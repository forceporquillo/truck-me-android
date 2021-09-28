package dev.forcecodes.truckme.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.extensions.findMapById

abstract class BaseMapFragment(
  @LayoutRes private val layoutId: Int
) : Fragment(layoutId), OnMapReadyCallback {

  private var supportMapFragment: SupportMapFragment? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    supportMapFragment = findMapById(R.id.admin_support_map)
    supportMapFragment?.getMapAsync(this)
  }

  override fun onStart() {
    super.onStart()
    supportMapFragment?.onStart()
  }

  override fun onResume() {
    super.onResume()
    supportMapFragment?.onResume()
  }

  override fun onPause() {
    super.onPause()
    supportMapFragment?.onPause()
  }

  override fun onStop() {
    super.onStop()
    supportMapFragment?.onStop()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    supportMapFragment?.onDestroyView()
  }

  override fun onDestroy() {
    super.onDestroy()
    supportMapFragment?.onDestroy()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    supportMapFragment?.onLowMemory()
  }
}