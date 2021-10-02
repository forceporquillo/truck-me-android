package dev.forcecodes.truckme.ui.fleet

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.databinding.FragmentAddVehicleBinding
import dev.forcecodes.truckme.extensions.attachProgressToMain
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.gallery.GalleryFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddVehicleFragment : GalleryFragment(R.layout.fragment_add_vehicle) {

  private val binding by viewBinding(FragmentAddVehicleBinding::bind)
  private val viewModel by viewModels<AddVehicleViewModel>()
  private val navArgs by navArgs<AddVehicleFragmentArgs>()

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    binding.submit.setOnClickListener {
      viewModel.submit()
    }

    val vehicleUri = navArgs.vehicleUri

    if (vehicleUri != null) {
     viewModel.vehicleUri = vehicleUri
      imageUrl = vehicleUri.profile
      bindProfileIcon(vehicleUri.profile) { profileInBytes ->
        viewModel.profileIconInBytes = profileInBytes
      }
    }

    binding.apply {
      vehicleName.textChangeObserver {
        viewModel!!.vehicleName = it
      }
      plateNumber.textChangeObserver {
        viewModel!!.plateNumber = it
      }
      description.textChangeObserver {
        viewModel!!.description = it
      }
    }

    repeatOnLifecycleParallel {
      launch {
        viewModel.showLoading.collect(::attachProgressToMain)
      }
      launch {
        viewModel.uploadState.collect {
          if (it is FleetUploadState.Success) {
            navigateUp()
          } else if (it is FleetUploadState.Error) {
            Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT)
              .show()
          }
        }
      }
    }
  }

  override fun requireGalleryViews(): GalleryViews {
    return binding.run {
      GalleryViews(driverProfile, avatar, addImageButton)
    }
  }

  override fun onImageSelectedResult(imageUri: Uri) {
    // viewModel
  }

  override fun onProfileChange(profileInBytes: ByteArray) {
    viewModel.setProfileInBytes(profileInBytes)
  }
}