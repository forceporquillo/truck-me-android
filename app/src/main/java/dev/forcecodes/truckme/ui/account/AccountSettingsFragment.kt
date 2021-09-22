package dev.forcecodes.truckme.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.databinding.FragmentAccountBinding
import dev.forcecodes.truckme.extensions.dispatchWhenBackPress
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.observeWithOnRepeatLifecycle
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.account.BackPressDispatcherUiActionEvent.Intercept
import dev.forcecodes.truckme.ui.account.BottomSheetNavActions.PasswordConfirmActions
import dev.forcecodes.truckme.ui.account.ConfirmPasswordChangeBottomSheet.Companion.TAG
import dev.forcecodes.truckme.ui.gallery.GalleryFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountSettingsFragment : GalleryFragment(R.layout.fragment_account) {

  private var confirmPasswordSheet: ConfirmPasswordChangeBottomSheet? = null
  private val settingsViewModel by viewModels<AccountSettingsViewModel>()
  private val binding by viewBinding(FragmentAccountBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.settingsViewModel = settingsViewModel

    binding.emailEt.setText(settingsViewModel.email)

    observeWithOnRepeatLifecycle {
      settingsViewModel.backPressUiEvent.collect { uiEvent ->
        val isInterceptUiEvent = uiEvent is Intercept
        dispatchWhenBackPress(isInterceptUiEvent) {
          if (isInterceptUiEvent) {
            showDiscardDialog()
          }
        }
      }
    }

    observeProfileChanges()
  }

  private fun observeProfileChanges() {
    repeatOnLifecycleParallel {
      launch {
        settingsViewModel.userInfo.collect { value: AuthenticatedUserInfoBasic? ->
          bindProfileIcon(value?.getPhotoUrl(), false) {
            settingsViewModel.profileIconInBytes = it
          }
        }
      }

      launch {
        settingsViewModel.passwordConfirmActions.collect { actions ->
          confirmPasswordSheet = ConfirmPasswordChangeBottomSheet()
          if (actions is PasswordConfirmActions) {
            confirmPasswordSheet?.show(childFragmentManager, TAG)
          } else {
            confirmPasswordSheet?.dismiss()
          }
        }
      }
    }
  }

  override fun requireGalleryViews(): GalleryViews {
    return binding.run {
      GalleryViews(profileIcon, avatar, addImageButton)
    }
  }

  override fun onImageSelectedResult(imageUri: Uri) {}

  override fun onStop() {
    binding.passwordEt.text?.clear()
    binding.confirmPasswordEt.text?.clear()

    super.onStop()
  }

  private fun showDiscardDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
      .setTitle(getString(R.string.unsave_changes))
      .setMessage(getString(R.string.settings_changes_dialog_message))
      .setPositiveButton(getString(R.string.save)) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      .setNeutralButton(getString(R.string.discard), null)
      .setOnDismissListener {
        navigateUp()
      }
      .show()
  }

  override fun onProfileChange(profileInBytes: ByteArray) {
    settingsViewModel.profileIconInBytes = profileInBytes
  }
}