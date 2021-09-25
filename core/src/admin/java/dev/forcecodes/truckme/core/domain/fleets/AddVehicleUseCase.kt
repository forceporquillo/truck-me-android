package dev.forcecodes.truckme.core.domain.fleets

import android.net.Uri
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetStorageDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.VehicleUri
import dev.forcecodes.truckme.core.data.fleets.VehicleByteArray
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.mapper.DomainMapper
import dev.forcecodes.truckme.core.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddVehicleUseCase @Inject constructor(
  private val fleetDataSource: FleetDataSource,
  private val fleetCloudStorage: FleetStorageDataSource,
  private val vehicleDomainMapper: VehicleDomainMapper,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<VehicleByteArray, VehicleResult>(ioDispatcher) {

  override fun execute(parameters: VehicleByteArray): Flow<Result<VehicleResult>> {
    return flow {
      emit(Result.Loading)
      // Step #1. Upload vehicle profile as byte array to GCP server if not empty.
      val uploadResult = parameters.profile?.run {
        val profileData = VEHICLE.getProfileData(parameters.id, this)
        fleetCloudStorage.uploadFleetProfile(profileData)
      }

      val vehicleAddResult = VehicleResult(data = parameters)
      var vehicleMapper: VehicleUri? = null

      // Step #2. Check if the upload result is success, so we can map our domain
      // [VehicleByteArray] object into [VehicleUri] object with uri attached as string.
      if (uploadResult is Result.Success) {
        uploadResult.data.data.let { uri ->
          if (uri == null) return@let
          vehicleMapper = vehicleDomainMapper.invoke(parameters, uri)
        }
      } else {
        vehicleAddResult.exception = uploadResult?.error
        Timber.e(uploadResult?.error.toString())
      }

      // Step #3. In this case. We'll just ignore the result of uploading and downloading
      // of profile URI. And just proceed suspending this method waiting for a one-shot
      // callback result of data. See [triggerOneShotListener].
      fleetDataSource.addVehicle(
        // Step #4. Save to NoSQL DB as collection reference. Otherwise, ignore.
        vehicleMapper ?: vehicleDomainMapper.invoke(parameters, null)
      ).triggerOneShotListener(vehicleAddResult)

      emit(Result.Success(vehicleAddResult))
    }
  }
}

@FleetDelegate
fun String.getProfileData(
  vehicleId: String,
  profileInBytes: ByteArray
): FleetProfileData {
  return FleetProfileData(this, ProfileData(vehicleId, profileInBytes))
}

@Singleton
class VehicleDomainMapper @Inject constructor(
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DomainMapper<VehicleByteArray, Uri?, VehicleUri> {

  override suspend fun invoke(
    from: VehicleByteArray,
    param: Uri?
  ): VehicleUri {
    return withContext(dispatcher) {
      from.run {
        VehicleUri(
          id = id,
          name = name,
          plate = plate,
          description = description,
          profile = param.toString(),
          isActive = isActive,
          assignedAdmin = from.assignedAdminId
        )
      }
    }
  }
}
