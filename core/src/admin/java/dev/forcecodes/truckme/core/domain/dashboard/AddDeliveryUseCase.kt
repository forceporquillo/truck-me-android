package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.model.DeliveryInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddDeliveryUseCase @Inject constructor(
  private val deliveryDataSource: AdminDeliveryDataSource,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FlowUseCase<DeliveryInfo, AddDeliveryTask>(dispatcher) {

  override fun execute(parameters: DeliveryInfo): Flow<Result<AddDeliveryTask>> {
    return flow {
      emit(Result.Loading)
      val result = deliveryDataSource.addDelivery(parameters)
        .triggerOneShotListener(AddDeliveryTask(null))!!
      if (result.isSuccess) {
        emit(Result.Success(result))
      } else
        emit(
          Result.Error(
            result.exception
              ?: Exception("Unknown error.")
          )
        )
    }
  }
}

data class AddDeliveryTask(
  override var data: Unit?,
  override var isSuccess: Boolean = false,
  override var exception: Exception? = null
) : TaskData<Unit>

data class DeliveryItems(
  val id: String,
  val timeStamp: String,
  val driverName: String,
  val driverId: String,
  val vehicleId: String,
  val destination: String,
  val profileIcon: String? = "",
  val eta: String
)