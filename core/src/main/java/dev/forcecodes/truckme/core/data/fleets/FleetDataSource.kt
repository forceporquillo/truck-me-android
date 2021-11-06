package dev.forcecodes.truckme.core.data.fleets

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.flow.Flow

interface FleetDataSource {
  fun addVehicle(data: VehicleUri): Task<Void>
  fun addDriver(data: DriverUri): Task<Void>
  fun observeVehicleChanges(): Flow<Result<List<VehicleUri>>>
  fun observeDriverChanges(): Flow<Result<List<DriverUri>>>
  fun onDeleteFleet(id: String, type: FleetType): Task<Void>
  fun onUpdateFleetState(id: String, state: Boolean, fleetType: FleetType)
}
