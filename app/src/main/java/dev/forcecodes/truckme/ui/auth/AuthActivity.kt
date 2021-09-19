package dev.forcecodes.truckme.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.databinding.ActivityAuthBinding
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.extensions.setUpGradientToolbar
import timber.log.Timber

interface AuthToolbarVisibilityListener {
  fun onShowToolbar(show: Boolean = false)
}

@AndroidEntryPoint
class AuthActivity : AppCompatActivity(), AuthToolbarVisibilityListener {

  private var _binding: ActivityAuthBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    _binding = ActivityAuthBinding.inflate(layoutInflater).apply {
      setContentView(root)
    }

    fillDecor(binding.toolbar.also { it.setUpGradientToolbar() })
    WindowCompat.setDecorFitsSystemWindows(window, false)
  }

  override fun onShowToolbar(show: Boolean) {
    Timber.e("Show $show")
    _binding?.toolbar?.alpha = if (show) 1f else 0f
  }

  override fun onSupportNavigateUp(): Boolean {
    return binding.navHostContainer.findNavController().navigateUp()
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}