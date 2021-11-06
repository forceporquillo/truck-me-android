package dev.forcecodes.truckme.core.domain.settings

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Error
import dev.forcecodes.truckme.core.util.Result.Success
import dev.forcecodes.truckme.core.util.data
import dev.forcecodes.truckme.core.util.driverCollection
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDriverProfileUseCase @Inject constructor(
  private val firestore: FirebaseFirestore,
  private val registeredDriverDataSource: RegisteredDriverDataSource,
  @IoDispatcher val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, String>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<String>> {
    return callbackFlow {
      val profileListenerRegistration = firestore.driverCollection()
        .document(registeredDriverDataSource.getUUIDbyAuthId(parameters).first().data ?: "")
        .addSnapshotListener { value, error ->
          if (value?.exists() == true) {
            val driverUri = value.toObject<DriverUri>()
            tryOffer(Success(driverUri?.profile ?: ""))
          } else {
            tryOffer(Error(Exception(error?.message)))
          }
        }
      awaitClose { profileListenerRegistration.remove() }
    }
  }
}