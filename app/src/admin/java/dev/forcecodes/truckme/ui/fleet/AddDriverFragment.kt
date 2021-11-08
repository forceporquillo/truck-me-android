package dev.forcecodes.truckme.ui.fleet

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentAddDriverBinding
import dev.forcecodes.truckme.extensions.navigate
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.gallery.GalleryFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddDriverFragment : GalleryFragment(R.layout.fragment_add_driver) {

  private val binding by viewBinding(FragmentAddDriverBinding::bind)
  private val viewModel by viewModels<AddDriverViewModel>()
  private val navArgs by navArgs<AddDriverFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    val driverUri = navArgs.driverUri

    if (driverUri != null) {
      viewModel.driverUri = driverUri
      bindProfileIcon(driverUri.profile) { profileInBytes ->
        viewModel.profileInBytes = profileInBytes
      }

      binding.password.isGone = true
      binding.confirmPassword.isGone = true
      binding.changePasswordBtn.isVisible = true

      binding.changePasswordBtn.setOnClickListener {
        navigate(AddDriverFragmentDirections.toDriverChangePassword(driverUri))
      }
    }

    binding.apply {
      fullNameEt.textChangeObserver(viewModel!!::fullName)
      emailEt.textChangeObserver(viewModel!!::email)
      passwordEt.textChangeObserver(viewModel!!::password)
      confirmPasswordEt.textChangeObserver(viewModel!!::confirmPassword)
      phoneNumberEt.textChangeObserver(viewModel!!::contactNumber)
    }

    repeatOnLifecycleParallel {
      launch {
        viewModel.enableSubmitButton.collect {
          binding.submit.isEnabled = it
        }
      }
      launch {
        viewModel.invalidContactNumber.collect {
          binding.phoneNumber.error = it
        }
      }
      launch {
        viewModel.invalidEmail.collect {
          binding.email.error = it
        }
      }
      launch {
        viewModel.passwordNotMatch.collect {
          binding.confirmPassword.error = it
        }
      }
      launch {
        viewModel.uploadState.collect {
          if (it is FleetUploadState.Success) {
            navigateUp()
          } else if (it is FleetUploadState.Error) {
            Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    binding.submit.setOnClickListener {
      viewModel.submit()
    }
  }

  override fun onProfileChange(profileInBytes: ByteArray) {
    viewModel.profileIconInBytes(profileInBytes)
  }

  override fun requireGalleryViews(): GalleryViews {
    return binding.run {
      GalleryViews(driverProfile, avatar, addImageButton)
    }
  }

  override fun onImageSelectedResult(imageUri: Uri) {}
}
