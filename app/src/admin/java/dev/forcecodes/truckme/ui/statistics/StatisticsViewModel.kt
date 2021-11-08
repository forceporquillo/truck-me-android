package dev.forcecodes.truckme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.DeliveredItemDataSource
import dev.forcecodes.truckme.core.data.delivery.convertToDate
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.fleets.DailyStatisticsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
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

  private val _driverList = MutableStateFlow<List<String>>(emptyList())
  val driverList = _driverList.asStateFlow()

  private val _dateList = MutableStateFlow<List<String>>(emptyList())
  val dateList = _dateList.asStateFlow()

  private val _itemDelivered = MutableStateFlow<List<String>>(emptyList())
  val itemDelivered = _itemDelivered.asStateFlow()

  private val _deliveryInfo = MutableStateFlow<List<DeliveryInfo>>(emptyList())

  private val _copyDeliveryInfo = MutableStateFlow<List<DeliveryInfo>>(emptyList())
  val copyDeliveryInfo = _copyDeliveryInfo.asStateFlow()

  init {
    loadStatistics()
  }

  fun loadStatistics() {
    type = DAY
    viewModelScope.launch {
      launch source@{
        deliveredItemDataSource.getAllDeliveredItems(userIdValue ?: return@source).map { list ->
          _dateList.value = list.map { deliveredItem ->
            convertToDate(
              "MM/dd/yyyy",
              timeStampMillis = deliveredItem.timestamp
            ) ?: ""
          }
          list
        }.collect {
          _deliveryInfo.value = it
        }
      }
      launch fleets@{
        observeDriverFleetsUseCase(userIdValue ?: return@fleets).collect {
          _driverList.mapFleetList(it)
        }
      }
    }
  }

  fun reOrder() {
    _itemDelivered.value = emptyList()

    if (type == DAY) {
      _itemDelivered.value =
        _deliveryInfo.value.map { convertToDate("MM/dd/yyyy", it.timestamp) ?: "" }.distinct()
    }

    if (type == DRIVER) {
      _itemDelivered.value = driverList.value
    }

    Timber.e("items ${_itemDelivered.value}")
  }

  private fun MutableStateFlow<List<String>>.mapFleetList(result: Result<List<DriverUri>>) {
    value = if (result is Result.Success) result.data.map { it.fullName } else emptyList()
  }

  fun executeQuery(newItem: String) {

    if (type == DAY) {
      _copyDeliveryInfo.value = _deliveryInfo.value.filter { convertToDate("MM/dd/yyyy", it.timestamp) == newItem }
    }

    if (type == DRIVER) {
      _copyDeliveryInfo.value = _deliveryInfo.value.filter { it.driverData?.driverName == newItem }
    }
    Timber.e("items ${_copyDeliveryInfo.value}")
  }

  companion object {
    private const val DAY = "Day"
    private const val DRIVER = "Driver"
  }

  override fun onCleared() {
    super.onCleared()
    Timber.e("onCleared()")
  }
}