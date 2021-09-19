package dev.forcecodes.truckme

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.databinding.ActivityMainBinding
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.extensions.setUpGradientToolbar
import dev.forcecodes.truckme.routes.topDestinations
import timber.log.Timber

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

    if (ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
    ) {
    } else {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        100
      );
    }

    WindowCompat.setDecorFitsSystemWindows(window, false)

    fillDecor(binding.materialToolbar.also { it.setUpGradientToolbar() })

    setupNavBar(binding.navigationView)
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

  fun test() {
    val destinationListener: ((NavController, NavDestination, Bundle?) -> Unit) =
      { _, navDestination, _ ->

        if (navDestination.id == R.id.accountFragment) {
          Timber.e("trueee")
        }
      }

    //navController.removeOnDestinationChangedListener(destinationListener)
    navController.addOnDestinationChangedListener(destinationListener)
  }
}