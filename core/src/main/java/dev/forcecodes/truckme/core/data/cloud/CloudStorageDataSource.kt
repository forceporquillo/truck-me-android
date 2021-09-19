package dev.forcecodes.truckme.core.data.cloud

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.util.uploadProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageDataSourceImpl @Inject constructor(
  private val firebaseStorage: FirebaseStorage
) : CloudStorageDataSource {

  override suspend fun uploadProfile(profileData: ProfileData): UploadTask {
    return firebaseStorage.reference.uploadProfile(profileData)
  }

  override fun getDownloadLink(): StorageReference {
    return firebaseStorage.reference
  }
}

interface CloudStorageDataSource {
  fun getDownloadLink(): StorageReference
  suspend fun uploadProfile(profileData: ProfileData): UploadTask
}