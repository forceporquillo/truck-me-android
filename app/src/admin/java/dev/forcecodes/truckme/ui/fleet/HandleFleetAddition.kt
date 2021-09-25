package dev.forcecodes.truckme.ui.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.error
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal fun <T: TaskData<*>> ViewModel.handleFleetAddition(
  useCase: Flow<Result<T>>,
  state: (FleetUploadState, Boolean) -> Unit = { _: FleetUploadState, _: Boolean -> }
) {
  viewModelScope.launch {
    useCase.collect { result ->
      when (result) {
        is Result.Loading -> state(FleetUploadState.Loading, true)
        is Result.Success -> {
          if (result.data.isSuccess) {
            state(FleetUploadState.Success, false)
          } else {
            state(FleetUploadState.Error(result.error), false)
          }
        }
        is Result.Error -> state(FleetUploadState.Error(result.error), false)
      }
    }
  }
}