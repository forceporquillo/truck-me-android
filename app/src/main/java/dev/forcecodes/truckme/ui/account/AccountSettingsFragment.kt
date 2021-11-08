package dev.forcecodes.truckme.ui.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.LauncherActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.databinding.FragmentAccountBinding
import dev.forcecodes.truckme.extensions.dispatchWhenBackPress
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.account.BackPressDispatcherUiActionEvent.Intercept
import dev.forcecodes.truckme.ui.account.BottomSheetNavActions.PasswordConfirmActions
import dev.forcecodes.truckme.ui.account.ConfirmPasswordChangeBottomSheet.Companion.TAG
import dev.forcecodes.truckme.ui.auth.signin.AdminAuthState
import dev.forcecodes.truckme.ui.gallery.GalleryFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

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
    binding.logout.setOnClickListener { logout() }

    observeOnLifecycleStarted {
      settingsViewModel.backPressUiEvent.collect { uiEvent ->
        val isInterceptUiEvent = uiEvent is Intercept
        dispatchWhenBackPress(isInterceptUiEvent) {
          if (isInterceptUiEvent) {
            showDiscardDialog()
          }
        }
      }
    }
  }

  override fun onResume() {
    repeatOnLifecycleParallel {
      launch {
        if (isDriver) {
          settingsViewModel.profileUri.collect { uri: Uri? ->
            bindProfileIcon(uri ?: return@collect, false) {
              settingsViewModel.profileIconInBytes = it
            }
          }
        } else {
          settingsViewModel.userInfo.collect { value: AuthenticatedUserInfoBasic? ->
            bindProfileIcon(value?.getPhotoUrl() ?: return@collect, false) {
              settingsViewModel.profileIconInBytes = it
            }
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
      launch {
        settingsViewModel.signInNavigationActions.collect {
          if (it is AdminAuthState.SignedOut) {
            startActivity(Intent(requireActivity(), LauncherActivity::class.java))
            requireActivity().finishAffinity()
          }
        }
      }
    }
    super.onResume()
  }

  override fun requireGalleryViews(): GalleryViews {
    return binding.run {
      GalleryViews(profileIcon, avatar, addImageButton)
    }
  }

  override fun onImageSelectedResult(imageUri: Uri) {
    //binding.avatar.bindProfileIcon(imageUri)
  }

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

  private fun logout() {
    FirebaseAuth.getInstance()
      .signOut()
  }

  override fun onProfileChange(profileInBytes: ByteArray) {
    Timber.e("onProfileChange")
    settingsViewModel.profileIconInBytes = profileInBytes
  }
}