package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.settings.UpdatePasswordV2UseCase
import dev.forcecodes.truckme.core.domain.settings.UpdatedPasswordV2
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.core.util.tryOffer
import dev.forcecodes.truckme.databinding.FragmentDriverPasswordChangeBinding
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.observeOnLifecycleStarted
import dev.forcecodes.truckme.extensions.textChangeObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DriverPasswordChangeFragment : Fragment(R.layout.fragment_driver_password_change) {

  private val viewModel by viewModels<DriverPasswordChangeViewModel>()
  private val navArgs by navArgs<DriverPasswordChangeFragmentArgs>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val binding = FragmentDriverPasswordChangeBinding.bind(view)

    binding.passwordEt.textChangeObserver {
      viewModel.newPassword = it
    }

    binding.confirmPasswordEt.textChangeObserver {
      viewModel.confirmNewPassword = it
    }

    observeOnLifecycleStarted {
      viewModel.enableSubmit.collect {
        binding.submit.isEnabled = it
      }
    }

    binding.submit.setOnClickListener {
      viewModel.submit(navArgs.driverUri ?: return@setOnClickListener)
    }

    observeOnLifecycleStarted {
      viewModel.isSuccess.collect {
        if (it) {
          navigateUp()
        }
      }
    }
  }
}