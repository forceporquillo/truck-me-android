package dev.forcecodes.truckme.core.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetType
import dev.forcecodes.truckme.core.data.fleets.FleetType.DRIVER
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.driverCollection
import dev.forcecodes.truckme.core.util.vehicleCollection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateMyFleetState @Inject constructor(
  private val firestore: FirebaseFirestore
) : FleetDataSource {

  override fun addVehicle(data: VehicleUri): Task<Void> {
    throw UnsupportedOperationException()
  }

  override fun addDriver(data: DriverUri): Task<Void> {
    throw UnsupportedOperationException()
  }

  override fun observeVehicleChanges(): Flow<Result<List<VehicleUri>>> {
    throw UnsupportedOperationException()
  }

  override fun observeDriverChanges(): Flow<Result<List<DriverUri>>> {
    throw UnsupportedOperationException()
  }

  override fun onDeleteFleet(id: String, type: FleetType): Task<Void> {
    throw UnsupportedOperationException()
  }

  override fun onUpdateFleetState(id: String, state: Boolean, fleetType: FleetType) {
    val fleetDocument = if (fleetType == DRIVER) {
      firestore.driverCollection()
    } else {
      firestore.vehicleCollection()
    }
    fleetDocument
      .document(id)
      .update(mapOf("hasOngoingDeliveries" to state))
  }
}