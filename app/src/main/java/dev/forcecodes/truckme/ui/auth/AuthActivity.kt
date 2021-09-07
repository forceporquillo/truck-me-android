package dev.forcecodes.truckme.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import dev.forcecodes.truckme.databinding.ActivityAuthBinding
import dev.forcecodes.truckme.extensions.doOnApplyWindowInsets
import dev.forcecodes.truckme.extensions.fillDecor
import dev.forcecodes.truckme.extensions.setUpGradientToolbar

interface AuthToolbarVisibilityListener {
    fun onShowToolbar(show: Boolean = false)
}

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
        _binding?.toolbar?.alpha = if (show) 1f else 0f
    }

    override fun onSupportNavigateUp(): Boolean {
        return binding.navHostContainer.findNavController().navigateUp()
    }
}

class AuthViewModel : ViewModel() {

    var isPaddingStateSave = false

}