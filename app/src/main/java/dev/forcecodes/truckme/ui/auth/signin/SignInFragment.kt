package dev.forcecodes.truckme.ui.auth.signin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentSignInBinding
import dev.forcecodes.truckme.extensions.createIntent
import dev.forcecodes.truckme.extensions.navigate
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.requireActivity
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

  private val viewModel by activityViewModels<FirebaseSignInViewModel>()
  private val binding by viewBinding(FragmentSignInBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    repeatOnLifecycleParallel {
      launch {
        viewModel.signInNavigationActions.collect { authState ->
          if (authState is AdminAuthState.SignedIn) {
            requireActivity {
              createIntent(MainActivity::class, finish = true)
            }
          }
        }
      }

      launch {
        viewModel.signInNavActions.collect { actions ->
          if (actions is SignInNavActions.ResetPasswordAction) {
            navigate(R.id.resetPasswordFragment)
          }
        }
      }
    }
  }
}


