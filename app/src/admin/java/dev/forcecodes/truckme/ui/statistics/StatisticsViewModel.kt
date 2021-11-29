package dev.forcecodes.truckme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.fleets.DeliveredItemStatsUseCase
import dev.forcecodes.truckme.core.domain.fleets.ObserveDriverFleetsUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.successOr
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
  private val observeDriverFleetsUseCase: ObserveDriverFleetsUseCase,
  private val deliveredItemStatsUseCase: DeliveredItemStatsUseCase,
  signInViewModelDelegate: SignInViewModelDelegate,
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  var type: String = DAY

  private val _driverList = MutableStateFlow<List<String>>(emptyList())
  val driverList = _driverList.asStateFlow()

  private val _dateList = MutableStateFlow<List<String>>(emptyList())
  val dateList = _dateList.asStateFlow()

  private val _itemDelivered = MutableStateFlow<List<String>>(emptyList())
  val itemDelivered = _itemDelivered.asStateFlow()

  private val _deliveryInfo = MutableStateFlow<List<ItemDeliveredStats>>(emptyList())

  private val _copyDeliveryInfo = MutableStateFlow<List<ItemDeliveredStats>>(emptyList())
  val copyDeliveryInfo = _copyDeliveryInfo.asStateFlow()

  init {
    loadStatistics()
  }

  fun loadStatistics() {
    type = DAY
    viewModelScope.launch {
      launch source@{
        deliveredItemStatsUseCase(
          userIdValue ?: return@source
        ).map { value: Result<List<ItemDeliveredStats>> ->
          val list = value.successOr(emptyList())
          _dateList.value = list.sortedByDescending {
            it.metadata.date
          }.map {
            it.metadata.date.orEmpty()
          }
          list
        }.collect {
          _deliveryInfo.value = it
          if (it.isNotEmpty()) {
            executeQuery(it[0].dateAccomplish ?: "")
          } else {
            _copyDeliveryInfo.value = emptyList()
          }
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
        _deliveryInfo.value.map { it.metadata.date ?: "" }.distinct()
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
      _copyDeliveryInfo.value = _deliveryInfo.value.filter { it.dateAccomplish == newItem }
    }

    if (type == DRIVER) {
      _copyDeliveryInfo.value = _deliveryInfo.value.filter { it.metadata.driverName == newItem }
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