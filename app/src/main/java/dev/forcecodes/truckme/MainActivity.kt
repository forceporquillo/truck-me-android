package dev.forcecodes.truckme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.databinding.ActivityMainBinding
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.extensions.setUpGradientToolbar

private val topDestinations = setOf(
    R.id.home,
    R.id.fleet,
    R.id.statistics,
    R.id.history,
    R.id.account
)

class MainActivity : AppCompatActivity() {

    private val appBarConfiguration = AppBarConfiguration(topDestinations)

    private val navController: NavController by lazy { navHostFragment.navController }

    private val navHostFragment: NavHostFragment
        get() = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.run {
            fillDecor(materialToolbar)
            materialToolbar.setUpGradientToolbar()
            navigationView.setupWithNavController(navController)
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}