package dev.forcecodes.truckme.core.domain.dashboard

import dev.forcecodes.truckme.core.data.delivery.AdminDeliveryDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteJobUseCase @Inject constructor(
  private val deliveryDataSource: AdminDeliveryDataSource,
  @IoDispatcher private val dispatcher: CoroutineDispatcher
): UseCase<String, Unit>(dispatcher) {

  override suspend fun execute(parameters: String) {
    deliveryDataSource.deleteJobById(parameters)
  }
}

data class UpdateFleetStateId(
  val documentId: String,
  val driverId: String,
  val vehicleId: String
)