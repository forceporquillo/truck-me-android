package dev.forcecodes.truckme.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.db.Notification
import dev.forcecodes.truckme.core.domain.notification.NotificationManager
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.successOr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveJobsViewModel @Inject constructor(
  private val deliveryDataSource: AdminDeliveryDataSource,
  private val notificationManager: NotificationManager
) : ViewModel() {

  private val _deliveryInfo = MutableStateFlow<DeliveryInfo?>(null)
  val deliveryInfo = _deliveryInfo.asStateFlow()

  private var id: String? = null

  fun getJob(id: String) {
    this.id = id
    viewModelScope.launch {
      deliveryDataSource.getActiveJobById(id).collect {
        _deliveryInfo.value = it.successOr(null)
      }
    }
  }

  fun notifyWhenDelivered() {
    notificationManager.setNotification(Notification(id ?: return))
  }
}
