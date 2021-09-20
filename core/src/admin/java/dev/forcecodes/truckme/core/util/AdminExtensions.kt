package dev.forcecodes.truckme.core.util

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.domain.fleets.FleetProfileData
import kotlinx.coroutines.tasks.await

suspend fun FirebaseStorage.downloadFleetDelegateProfile(
  fleetProfileData: FleetProfileData
): DownloadUrlResult {
  val (fleetType, profileData) = fleetProfileData
  val downloadResult = DownloadUrlResult()
  reference.child("fleets")
    .child(fleetType)
    .child(profileData.userId!!)
    .child(PROFILE)
    .downloadUrl
    .addOnCompleteListener {
      downloadResult.isSuccess = it.isSuccessful
      downloadResult.exception = it.exception
      downloadResult.data = it.result
    }.await()
  return downloadResult
}

fun FirebaseStorage.uploadFleetDelegateProfile(
  fleetProfileData: FleetProfileData
): UploadTask {
  val (fleetType, profileData) = fleetProfileData
  return reference.child("fleets")
    .child(fleetType)
    .child(profileData.userId!!)
    .child(PROFILE)
    .putBytes(profileData.profileIconInBytes!!)
}