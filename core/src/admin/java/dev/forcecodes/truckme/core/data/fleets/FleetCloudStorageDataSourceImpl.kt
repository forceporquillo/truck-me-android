package dev.forcecodes.truckme.core.data.fleets

import com.google.firebase.storage.FirebaseStorage
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.fleets.FleetProfileData
import dev.forcecodes.truckme.core.domain.settings.UploadResult
import dev.forcecodes.truckme.core.util.DownloadUrlResult
import dev.forcecodes.truckme.core.util.FleetDelegate
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.downloadFleetDelegateProfile
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import dev.forcecodes.truckme.core.util.uploadFleetDelegateProfile
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FleetCloudStorageDataSourceImpl @Inject constructor(
  private val firebaseStorage: FirebaseStorage
) : FleetStorageDataSource {

  override suspend fun uploadFleetProfile(
    fleetData: FleetProfileData
  ): Result<DownloadUrlResult> {
    val uploadResult = UploadResult(data = fleetData.profileData)
    firebaseStorage.uploadFleetDelegateProfile(fleetData)
      .triggerOneShotListener(uploadResult)

    return if (!uploadResult.isSuccess) {
      return Result.Error(
        Exception("Error uploading profile in bytes of ${fleetData.profileData.userId}")
      )
    } else {
      val downloadResult = downloadProfileUri(fleetData)

      if (downloadResult.isSuccess) {
        Result.Success(downloadResult)
      } else {
        Result.Error(
          downloadResult.exception
            ?: Exception("Error downloading profile uri of ${fleetData.profileData.userId}")
        )
      }
    }
  }

  override suspend fun downloadFleetProfile(delegate: String) {
    // listen to changes and update
  }

  private suspend fun downloadProfileUri(
    fleetData: FleetProfileData
  ): DownloadUrlResult {
    return firebaseStorage.downloadFleetDelegateProfile(fleetData)
  }
}

interface FleetStorageDataSource {
  suspend fun uploadFleetProfile(fleetData: FleetProfileData): Result<DownloadUrlResult>
  suspend fun downloadFleetProfile(@FleetDelegate delegate: String)
}
