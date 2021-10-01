package dev.forcecodes.truckme.core.data.fleets

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.forcecodes.truckme.core.data.fleets.FleetType.DRIVER
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.util.deleteCollection
import dev.forcecodes.truckme.core.util.driverCollection
import dev.forcecodes.truckme.core.util.fleetSnapshots
import dev.forcecodes.truckme.core.util.vehicleCollection
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FleetsDataSourceImpl @Inject constructor(
  val firestore: FirebaseFirestore
) : FleetDataSource {

  override fun addVehicle(data: VehicleUri): Task<Void> {
    return firestore.vehicleCollection()
      .document(data.id)
      .set(data)
  }

  override fun addDriver(data: DriverUri): Task<Void> {
    return firestore.driverCollection()
      .document(data.id)
      .set(data)
  }

  override fun observeVehicleChanges() = firestore.vehicleCollection().fleetSnapshots<VehicleUri>()
  override fun observeDriverChanges() = firestore.driverCollection().fleetSnapshots<DriverUri>()
  override fun onDeleteFleet(id: String, type: FleetType) = firestore.deleteCollection(id, type)
}



