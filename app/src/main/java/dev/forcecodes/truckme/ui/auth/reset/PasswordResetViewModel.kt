package dev.forcecodes.truckme.ui.auth.reset

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.domain.settings.PasswordReset
import dev.forcecodes.truckme.core.domain.settings.PasswordResetUseCase
import dev.forcecodes.truckme.ui.auth.BaseAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
  private val passwordResetUseCase: PasswordResetUseCase
) : BaseAuthViewModel<UiActionEvent>() {

  private val _passwordResetSuccess = MutableStateFlow(PasswordReset())
  val passwordResetSuccess = _passwordResetSuccess.asStateFlow()

  fun submitClick() {
    submitAndSetLoading()
    email?.let { email ->
      reset(email)
    }
  }

  private fun reset(email: String) {
    viewModelScope.launch {
      fetchResult(passwordResetUseCase(email)) { data, _ ->
        _passwordResetSuccess.value = data ?: return@fetchResult
      }
    }
  }
}