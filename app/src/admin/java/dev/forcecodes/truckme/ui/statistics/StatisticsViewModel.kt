package dev.forcecodes.truckme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.fleets.AdminDailySales
import dev.forcecodes.truckme.core.domain.fleets.DailyStatisticsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.domain.fleets.formattedDate
import dev.forcecodes.truckme.core.model.ItemDelivered
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
  private val deliveredItemDataSource: DeliveredItemDataSource,
  private val observeDriverFleetsUseCase: ObserveDriverFleetsUseCase,
  private val dailyStatisticsUseCase: DailyStatisticsUseCase,
  signInViewModelDelegate: SignInViewModelDelegate,
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  var type: String = DAY
    set(value) {
      field = value
      searchBy(userIdValue ?: return)
    }

  private val _driverList = MutableStateFlow<List<String>>(emptyList())
  val driverList = _driverList.asStateFlow()

  private val _dateList = MutableStateFlow<List<String>>(emptyList())
  val dateList = _dateList.asStateFlow()

  private val _itemDelivered = MutableStateFlow<List<ItemDelivered>>(emptyList())
  val itemDelivered = _itemDelivered.asStateFlow()

  fun searchBy(field: String) {
    viewModelScope.launch {
      if (type == DAY) {
        _driverList.value = emptyList()
        dailyStatisticsUseCase(AdminDailySales(userIdValue ?: return@launch, field)).collect {
          Timber.e("Result $it")
          if (it is Result.Success) {
            _dateList.value = if (it.data.isEmpty()) {
              listOf(formattedDate(System.currentTimeMillis()))
            } else {
              it.data
            }
          }
          if (it is Result.Error) {
            _dateList.value = listOf(formattedDate(System.currentTimeMillis()))
          }
        }
      }
      if (type == DRIVER) {
        _dateList.value = emptyList()
        observeDriverFleetsUseCase(userIdValue ?: return@launch).collect { result ->
          _driverList.mapFleetList(result)
        }
      }
    }
  }

  fun executeQuery(newItem: String) {
    _itemDelivered.value = emptyList()
    viewModelScope.launch {
      deliveredItemDataSource.getAllDeliveredItems(userIdValue ?: return@launch).collect {
        if (type == DAY) {
          _itemDelivered.value =
            it.filter { info -> formattedDate(info.timestamp.toLong()) == newItem }
        }
        if (type == DRIVER) {
          _itemDelivered.value =
            it.filter { info -> info.deliveryInfo?.driverData?.driverName == newItem }
        }
      }
    }
  }

  private fun MutableStateFlow<List<String>>.mapFleetList(result: Result<List<DriverUri>>) {
    value = if (result is Result.Success) result.data.map { it.fullName } else emptyList()
  }

  companion object {
    private const val DAY = "Day"
    private const val DRIVER = "Driver"
  }
}