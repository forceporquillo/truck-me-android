package dev.forcecodes.truckme.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.places.PlaceAutoCompleteResponse
import dev.forcecodes.truckme.core.data.places.Prediction
import dev.forcecodes.truckme.core.domain.places.SearchPlacesUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.cancelIfActive
import dev.forcecodes.truckme.core.util.data
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchPlacesViewModel @Inject constructor(
  private val searchPlacesUseCase: SearchPlacesUseCase
) : ViewModel() {

  private val _placesResults = MutableStateFlow<List<Places>>(emptyList())
  val placesResults = _placesResults.asStateFlow()

  private var searchJob: Job? = null

  private val placeQuery = MutableStateFlow("")

  private val _emptyResponse = MutableStateFlow<String?>("")
  val emptyResponse = _emptyResponse.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _isEmpty = MutableStateFlow(false)
  val isEmpty = _isEmpty.asStateFlow()

  fun searchPlace(place: String) {
    val newQuery = place.trim().takeIf { it.length >= 2 } ?: ""
    if (placeQuery.value != newQuery) {
      placeQuery.value = newQuery
      executeSearchingPlace()
    }
  }

  private fun executeSearchingPlace() {
    searchJob?.cancelIfActive()

    if (placeQuery.value.isEmpty()) {
      clearSearchResults()
      return
    }

    searchJob = viewModelScope.launch {
      placeQuery
        .debounce(250L)
        .collect { query ->
          searchPlacesUseCase.invoke(query).collect(::processSearchResult)
        }
    }
  }

  private fun processSearchResult(searchPredictions: Result<PlaceAutoCompleteResponse>) {
    if (searchPredictions is Result.Loading) {
      _isLoading.value = true
      return
    }

    val predictions = searchPredictions.data?.predictions ?: emptyList()
    _placesResults.value = formatPlaceStructure(predictions)
    _isLoading.value = false
    _isEmpty.value = predictions.isEmpty()

    if (searchPredictions is Result.Error) {
      Timber.e(searchPredictions.exception.message)
      _emptyResponse.value = searchPredictions.exception.message
      _isEmpty.value = true
    }
  }

  private fun formatPlaceStructure(searchPredictions: List<Prediction>): List<Places> {
    return searchPredictions.run {
      map { predictions ->
        Places(
          predictions.placeId ?: "",
          predictions.structuredFormatting?.mainText ?: "",
          predictions.structuredFormatting?.secondaryText ?: ""
        )
      }
    }
  }

  private fun clearSearchResults() {
    _placesResults.value = emptyList()
    _isLoading.value = false
    _isEmpty.value = false
    _emptyResponse.value = ""
  }
}