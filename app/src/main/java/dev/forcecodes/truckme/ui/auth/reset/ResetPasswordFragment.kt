package dev.forcecodes.truckme.ui.auth.reset

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentResetPasswordBinding
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.auth.AuthToolbarVisibilityListener
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

  private val viewModel by viewModels<PasswordResetViewModel>()
  private val binding by viewBinding(FragmentResetPasswordBinding::bind)

  private var visibilityListener: AuthToolbarVisibilityListener? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    // show gradient toolbar
    context.isAuthToolbar()?.onShowToolbar(true)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.viewModel = viewModel
    binding.lifecycleOwner = viewLifecycleOwner

    observeOnLifecycleStarted {
      viewModel.passwordResetSuccess.collect { reset ->
        if (reset.isSuccess) {
          showSuccessDialog(reset.data.toString())
        }
      }
    }
  }

  private fun showSuccessDialog(email: String) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(getString(R.string.password_reset))
      .setMessage(getString(R.string.password_sent_message, email))
      .setPositiveButton(getString(R.string.okay_got_it)) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      .setOnDismissListener { findNavController().navigateUp() }
      .show()
  }

  override fun onDestroyView() {
    visibilityListener?.onShowToolbar(false)
    super.onDestroyView()
  }

  private fun Context.isAuthToolbar() = (this as? AuthToolbarVisibilityListener?)
    .apply { visibilityListener = this }
}

