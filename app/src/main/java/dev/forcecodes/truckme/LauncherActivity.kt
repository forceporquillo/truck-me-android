package dev.forcecodes.truckme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.ui.auth.AuthActivity
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    @Inject
    lateinit var signInViewModelDelegate: SignInViewModelDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                signInViewModelDelegate.isUserSignedIn
                    .debounce(500L)
                    .collectLatest { isSignedIn ->
                        val actions = if (isSignedIn) {
                            MainActivity::class
                        } else {
                            AuthActivity::class
                        }
                        navigationActions(actions)
                    }
            }
        }
    }

    private fun navigationActions(kClass: KClass<out AppCompatActivity>) {
        startActivity(Intent(this, kClass.java))
        finish()
    }
}