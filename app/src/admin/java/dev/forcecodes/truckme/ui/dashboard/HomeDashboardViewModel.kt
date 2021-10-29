package dev.forcecodes.truckme.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.domain.dashboard.ActiveJobOder
import dev.forcecodes.truckme.core.domain.dashboard.DeliveryItems
import dev.forcecodes.truckme.core.domain.dashboard.GetActiveJobsUseCase
import dev.forcecodes.truckme.core.domain.dashboard.GetOrder
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
  private val activeJobsUseCase: GetActiveJobsUseCase,
  private val signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel() {

  private val _activeJobsList = MutableStateFlow<List<DeliveryItems>>(emptyList())
  val activeJobsList = _activeJobsList.asStateFlow()

  val emptyList: StateFlow<Boolean> = _activeJobsList.map {
    it.isEmpty()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

  fun activeJobOrder(order: ActiveJobOder) {
      viewModelScope.launch {
        signInViewModelDelegate.userInfo.flatMapConcat { adminInfo ->
          val uidOrException = adminInfo?.getUid() ?: throw RuntimeException("yes daddy")
          val getOrder = GetOrder(uidOrException, order)
          activeJobsUseCase.invoke(getOrder)
        }.collect { result ->
          _activeJobsList.value = result.successOr(emptyList())
        }
    }
  }
}