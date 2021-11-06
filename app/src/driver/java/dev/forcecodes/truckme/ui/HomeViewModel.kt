package dev.forcecodes.truckme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.ActiveJobItems
import dev.forcecodes.truckme.core.domain.jobs.AssignedJobsUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  assignedJobsUseCase: AssignedJobsUseCase,
  signedInViewModelDelegate: SignInViewModelDelegate
) : ViewModel() {

  private val _assignedJobsList = MutableStateFlow(JobListUiModel())
  val assignedJobsList = _assignedJobsList.asStateFlow()

  init {
    viewModelScope.launch {
      val driverId = signedInViewModelDelegate.userIdValue ?: return@launch

      assignedJobsUseCase(driverId).collect { result ->
        _assignedJobsList.value = when (result) {
          is Result.Loading -> JobListUiModel()
          is Result.Success -> {
            JobListUiModel(
              isLoading = false,
              data = result.data,
              result.data.isEmpty()
            )
          }
          is Result.Error -> {
            JobListUiModel(isLoading = false, isEmpty = true)
          }
        }
      }
    }
  }

  data class JobListUiModel(
    val isLoading: Boolean = true,
    val data: List<ActiveJobItems> = emptyList(),
    val isEmpty: Boolean = false
  )
}
