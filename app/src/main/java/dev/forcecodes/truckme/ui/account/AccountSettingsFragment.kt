package dev.forcecodes.truckme.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.databinding.FragmentAccountBinding
import dev.forcecodes.truckme.extensions.*
import dev.forcecodes.truckme.ui.account.BackPressDispatcherUiActionEvent.*
import dev.forcecodes.truckme.ui.account.BottomSheetNavActions.*
import dev.forcecodes.truckme.ui.account.ConfirmPasswordChangeBottomSheet.Companion.TAG
import dev.forcecodes.truckme.ui.gallery.GalleryBottomSheet
import dev.forcecodes.truckme.ui.gallery.GalleryBottomSheet.Companion.GALLERY_TAG
import dev.forcecodes.truckme.ui.gallery.GalleryFragment
import dev.forcecodes.truckme.ui.gallery.GalleryViewModel
import dev.forcecodes.truckme.ui.gallery.Image
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AccountSettingsFragment : GalleryFragment(R.layout.fragment_account) {

    private var galleryBottomSheet: GalleryBottomSheet? = null
    private var confirmPasswordSheet: ConfirmPasswordChangeBottomSheet? = null

    private val settingsViewModel by viewModels<AccountSettingsViewModel>()
    private val imageViewModel by viewModels<GalleryViewModel>()

    private val binding by viewBinding(FragmentAccountBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.imageViewModel = imageViewModel
        binding.settingsViewModel = settingsViewModel

        binding.emailEt.setText(settingsViewModel.email)

        galleryBottomSheet = GalleryBottomSheet()

        binding.addImageButton.setOnClickListener(::showBottomSheetGallery)

        observeWithOnRepeatLifecycle {
            settingsViewModel.backPressUiEvent.collect { uiEvent ->
                Timber.e("uiEvent $uiEvent")
                val isInterceptUiEvent = uiEvent is Intercept
                dispatchWhenBackPress(isInterceptUiEvent) {
                    if (isInterceptUiEvent) {
                        showDiscardDialog()
                    }

                    (requireActivity() as MainActivity).test()
                }
            }
        }

        observeProfileChanges()
    }

    private fun observeProfileChanges() {
        repeatOnLifecycleParallel {
            launch {
                imageViewModel.image.collect { image: Image? ->
                    bindProfileIcon(image?.imageUri ?: image?.imageUrl)
                }
            }

            launch {
                settingsViewModel.userInfo.collect { value: AuthenticatedUserInfoBasic? ->
                    bindProfileIcon(value?.getPhotoUrl(), false)
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

    private fun <T> bindProfileIcon(data: T, dispatch: Boolean = true) {
        binding.profileIcon.bindProfileIcon(data) isReady@{ isReady ->
            binding.avatar.isGone = isReady

            if (!isReady) {
                return@isReady
            }

            if (dispatch) {
                // save profile bitmap everytime glide returns true
                saveCompressedBitmap()

                if (galleryBottomSheet?.isAdded == true) {
                    galleryBottomSheet?.dismiss()
                }
            }
        }
    }

    override fun onImageSelectedResult(imageUri: Uri) {}

    private fun saveCompressedBitmap() {
        // Suppress exception by placing our code to a runnable queue
        // and wait 'til Glide processes the image from the separate thread.
        // Save and store the compressed byte array image into settingsViewModel.
        with(binding.profileIcon) {
            post {
                settingsViewModel.profileIcon = compressAsBitmap()
            }
        }
    }

    private fun showBottomSheetGallery(view: View) {
        galleryBottomSheet = GalleryBottomSheet()
        view.disableButtonForAWhile {
            galleryBottomSheet?.show(childFragmentManager, GALLERY_TAG)
        }
        view.requestFocus()
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
            .setNegativeButton(getString(R.string.discard), null)
            .setOnDismissListener {
                navigateUp()
            }
            .show()
    }
}