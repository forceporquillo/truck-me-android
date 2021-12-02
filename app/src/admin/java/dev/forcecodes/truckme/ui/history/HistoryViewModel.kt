package dev.forcecodes.truckme.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.DeliveredItem
import dev.forcecodes.truckme.core.domain.history.HistoryUseCase
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
  private val historyUseCase: HistoryUseCase,
  signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  private val _historyList = MutableStateFlow<List<DeliveredItem>>(emptyList())
  val historyList = _historyList.asStateFlow()

  init {
    viewModelScope.launch {
      historyUseCase(userIdValue ?: return@launch).collect {
        _historyList.value = it.successOr(emptyList())
      }
    }
  }
}