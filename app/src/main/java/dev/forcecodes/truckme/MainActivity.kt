package dev.forcecodes.truckme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.databinding.ActivityMainBinding
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.extensions.applyTranslucentStatusBar
import dev.forcecodes.truckme.routes.topDestinations
import dev.forcecodes.truckme.util.PermissionUtils.requestMultiplePermissions

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val appBarConfiguration = AppBarConfiguration(topDestinations)

  private val navController: NavController by lazy { navHostFragment.navController }

  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  private val navHostFragment: NavHostFragment
    get() = supportFragmentManager.findFragmentById(
      R.id.nav_host_container
    ) as NavHostFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    binding.materialToolbar.apply {
      fillDecor(this)
      applyTranslucentStatusBar()
    }
    setupNavBar(binding.navigationView)
    requestMultiplePermissions()
  }

  private fun setupNavBar(navigationView: BottomNavigationView) {
    navigationView.setupWithNavController(navController)
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration)
  }

  fun showLoading(isLoading: Boolean) {
    binding.progressIndicator.isVisible = isLoading
  }

  fun getToolbar(): MaterialToolbar {
    return binding.materialToolbar
  }

}