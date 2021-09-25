package dev.forcecodes.truckme.core.domain.fleets

import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.fleets.*
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddDriverUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  private val fleetCloudStorage: FleetStorageDataSource,
  private val authStateDataSource: FirebaseAuthStateDataSource,
  private val registeredUserDataSource: RegisteredDriverDataSource,
  private val driverDomainMapper: DriverDomainMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<DriverByteArray, DriverResult>(ioDispatcher) {

  override fun execute(parameters: DriverByteArray): Flow<Result<DriverResult>> {
    return flow {
      emit(Result.Loading)
      // Step #1. Upload driver profile as byte array to GCP server if not empty.
      val uploadResult = parameters.profile?.run {
        val profileData = DRIVER.getProfileData(parameters.id, this)
        fleetCloudStorage.uploadFleetProfile(profileData)
      }

      val vehicleAddResult = DriverResult(data = parameters)
      var vehicleMapper: DriverUri? = null

      // Step #2. Check if the upload result is success, so we can map our domain
      // [VehicleByteArray] object into [VehicleUri] object with uri attached as string.
      if (uploadResult is Result.Success) {
        uploadResult.data.data.let { uri ->
          if (uri == null) return@let
          vehicleMapper = driverDomainMapper.invoke(parameters, uri)
        }
      } else {
        vehicleAddResult.exception = uploadResult?.error
        Timber.e(uploadResult?.error.toString())
      }

      // Step #3. In this case. We'll just ignore the result of uploading and downloading
      // of profile URI. And just proceed suspending this method waiting for a one-shot
      // callback result of data. See [triggerOneShotListener].
      fleetDataSource.addDriver(
        // Step #4. Save to NoSQL DB as collection reference. Otherwise, ignore.
        vehicleMapper ?: driverDomainMapper.invoke(parameters, null)
      ).triggerOneShotListener(vehicleAddResult)
      register(parameters.id)

      emit(Result.Success(vehicleAddResult))
    }
  }

  private fun register(userId: String) {
    registeredUserDataSource.register(userId)
  }
}

