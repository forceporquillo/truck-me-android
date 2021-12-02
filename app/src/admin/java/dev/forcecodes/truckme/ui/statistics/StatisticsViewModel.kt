package dev.forcecodes.truckme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.ItemDeliveredStats
import dev.forcecodes.truckme.core.domain.statistics.DAILY
import dev.forcecodes.truckme.core.domain.statistics.FilteredDailyStatisticsUseCase
import dev.forcecodes.truckme.core.domain.statistics.DeliveredItemStatsUseCase
import dev.forcecodes.truckme.core.domain.statistics.ForceRefreshUseCase
import dev.forcecodes.truckme.core.domain.statistics.QueryParams
import dev.forcecodes.truckme.core.domain.statistics.StatisticsSortType
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.successOr
import dev.forcecodes.truckme.core.util.tryOffer
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
  private val filteredDailyStatisticsUseCase: FilteredDailyStatisticsUseCase,
  private val deliveredItemStatsUseCase: DeliveredItemStatsUseCase,
  private val forceRefreshUseCase: ForceRefreshUseCase,
  signInViewModelDelegate: SignInViewModelDelegate,
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  private val _filterType = MutableStateFlow<List<String>>(emptyList())
  val filterType = _filterType.asStateFlow()

  private val _itemDeliveredStats = MutableStateFlow<List<ItemDeliveredStats>>(emptyList())
  val itemDeliveredStats = _itemDeliveredStats.asStateFlow()

  @StatisticsSortType
  private var sortFilterType: String = DAILY

  init {
    refresh()
    filterStatsBy(null)
    filterTypeBy()
  }

  fun filterTypeBy(@StatisticsSortType type: String = DAILY) {
    if (sortFilterType != type){
      sortFilterType = type
    }

    viewModelScope.launch {
      filteredDailyStatisticsUseCase(type).collect { result ->
        _filterType.value = result.successOr(emptyList()).also { list ->
          if (list.isNotEmpty()) {
            filterStatsBy(list.first())
          }
        }
      }
    }
  }

  private fun refresh() {
    viewModelScope.launch {
      forceRefreshUseCase(userIdValue ?: return@launch)
        .collect()
    }
  }

  var previousFilter: String? = ""

  fun filterStatsBy(field: String?) {
    if (previousFilter != field) {
      previousFilter = field
    }

    viewModelScope.launch {
      val userIdOrIgnore = userIdValue ?: return@launch
      val params = QueryParams(userIdOrIgnore, sortFilterType, field)

      deliveredItemStatsUseCase(params).collect { list ->
        _itemDeliveredStats.value = list.successOr(emptyList())
      }
    }
  }
}