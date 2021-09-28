package dev.forcecodes.truckme.core.domain.settings

import com.google.firebase.auth.ktx.userProfileChangeRequest
import dev.forcecodes.truckme.core.data.auth.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.cloud.CloudStorageDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.downloadUrl
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadProfileUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  private val cloudStorageDataSource: CloudStorageDataSource,
  @IoDispatcher val dispatcher: CoroutineDispatcher
) : UseCase<ProfileData, UploadResult>(dispatcher) {

  override suspend fun execute(parameters: ProfileData): UploadResult {
    val uploadResult = UploadResult(data = parameters)
    val result = cloudStorageDataSource
      .uploadProfile(parameters)
      .triggerOneShotListener(uploadResult)!!

    if (result.isSuccess) {
      setUserProfile(parameters.userId!!)
    } else {
      Timber.e(result.exception)
    }

    return result
  }

  private suspend fun setUserProfile(userId: String) {
    val downloadUriResult = cloudStorageDataSource
      .getDownloadLink()
      .downloadUrl(userId)

    downloadUriResult?.let { uri ->
      authStateDataSource.updateProfile(
        userProfileChangeRequest {
          photoUri = uri
        }
      )
    }
  }
}

data class UploadResult(
  override var data: ProfileData? = null,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<ProfileData>

data class ProfileData(
  val userId: String?,
  val profileIconInBytes: ByteArray?
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProfileData

    if (userId != other.userId) return false
    if (!profileIconInBytes.contentEquals(other.profileIconInBytes)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = userId.hashCode()
    result = 31 * result + profileIconInBytes.contentHashCode()
    return result
  }
}