package dev.forcecodes.truckme.ui.fleet

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentAddDriverBinding
import dev.forcecodes.truckme.extensions.ANIMATION_FAST_MILLIS_V2
import dev.forcecodes.truckme.extensions.attachProgressToMain
import dev.forcecodes.truckme.extensions.navigate
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.extensions.toast
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
      binding.phoneNumber.isErrorEnabled = true

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
      licenseNumberEt.textChangeObserver(viewModel!!::licenseNumber)
      restrictionEt.textChangeObserver(viewModel!!::restrictions)
      licenseExpirationEt.textChangeObserver(viewModel!!::licenseExpiration)
      licenseExpirationEt.setOnClickListener { showDatePicker() }
      licenseExpirationEt.setOnItemClickListener { _, _, _, _ -> showDatePicker() }
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
          binding.phoneNumber.isErrorEnabled = it.isNotEmpty()
        }
      }
      launch {
        viewModel.invalidEmail.collect {
          binding.email.error = it
          binding.email.isErrorEnabled = it?.isEmpty() == false
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
            navigateUp(ANIMATION_FAST_MILLIS_V2)
          } else if (it is FleetUploadState.Error) {
            Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
          }
        }
      }
      launch {
        viewModel.showLoading.collect(::attachProgressToMain)
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

  private fun showDatePicker() {
    val datePicker =
      MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select date")
        .build()

    datePicker.show(childFragmentManager, "date_picker")
    datePicker.addOnPositiveButtonClickListener {
      binding.licenseExpirationEt.setText(datePicker.headerText)
    }
  }
}
