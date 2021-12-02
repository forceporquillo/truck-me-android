package dev.forcecodes.truckme

sealed class ContactUiState {
  object Loading: ContactUiState()
  data class Success(val contact: String?): ContactUiState()
  data class Error(val error: String?): ContactUiState()
}
