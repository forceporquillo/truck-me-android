package dev.forcecodes.truckme.core.util

import android.net.Uri
import androidx.annotation.StringDef
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dev.forcecodes.truckme.core.data.fleets.EmptyFleetsException
import dev.forcecodes.truckme.core.data.fleets.FleetDelegate
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.util.Result.Error
import dev.forcecodes.truckme.core.util.Result.Success
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

// TODO migrate

internal const val PROFILE = "profile.jpg"
internal const val REFERENCE = dev.forcecodes.truckme.core.BuildConfig.FLAVOR

fun FirebaseFirestore.phoneNumberDocument(userId: String): DocumentReference {
  return collection(REFERENCE)
    .document(userId)
}

fun FirebaseFirestore.updatePhoneNumberDocument(
  userId: String,
  phoneNumber: PhoneNumber
): Task<Void> {
  return collection(REFERENCE).document(userId).set(phoneNumber)
}

fun FirebaseFirestore.fleetRegDocument(
  userId: String
): DocumentReference {
  return collection("fleets")
    .document("registration")
    .collection("driver")
    .document(userId)
}

fun FirebaseFirestore.driverCollection(): CollectionReference {
  return collection("fleets")
    .document("all")
    .collection("drivers")
}

fun FirebaseFirestore.vehicleCollection(): CollectionReference {
  return collection("fleets")
    .document("all")
    .collection("vehicles")
}

fun StorageReference.uploadProfile(
  profileData: ProfileData
): UploadTask {
  return child(REFERENCE)
    .child(profileData.userId!!)
    .child(PROFILE)
    .putBytes(profileData.profileIconInBytes!!)
}

suspend fun StorageReference.downloadUrl(
  userId: String
): Uri? {
  val downloadResult = DownloadUrlResult()
  return child(REFERENCE)
    .child(userId)
    .child(PROFILE)
    .downloadUrl
    .addOnCompleteListener {
      downloadResult.isSuccess = it.isSuccessful
      downloadResult.exception = it.exception
      downloadResult.data = it.result
    }.await()
}

data class DownloadUrlResult(
  override var data: Uri? = null,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<Uri>

// flavor as bucket or collection reference METADATA.
const val VEHICLE = "vehicle"
const val DRIVER = "driver"

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@StringDef(value = [VEHICLE, DRIVER])
annotation class FleetDelegate

interface TaskData<T> {
  var isSuccess: Boolean
  var exception: Exception?
  var data: T?
}

suspend inline fun <T : TaskData<U>, U, V> Task<V>.triggerOneShotListener(
  data: T? = null,
  noinline block: ((t: Task<V>, e: Exception?) -> Unit)? = null
): T? = data.run {
  addOnCompleteListener { result ->
    val exceptionOrNull = !result.isSuccessful then result.exception
    block?.invoke(result, exceptionOrNull)
    data?.apply {
      isSuccess = result.isSuccessful
      exception = result.exception
    }
  }.await()
  this
}

infix fun <T> Boolean.then(param: T): T? = if (this) param else null

inline fun <reified T : Any>
  CollectionReference.fleetSnapshots():
  Flow<Result<List<T>>> = callbackFlow {
  val fleetSubscriptionListener = this@fleetSnapshots
    .addSnapshotListener { snapshot: QuerySnapshot?, e ->
      if (snapshot?.isEmpty == false) {
        val fleetList = mutableListOf<T>()
        snapshot.forEach { data ->
          val fleetData = data.toObject<T>()
          fleetList.add(fleetData)
        }
        tryOffer(Success(fleetList))
      } else {
        tryOffer(Error(EmptyFleetsException(e)))
      }
      Unit
    }
  awaitClose { fleetSubscriptionListener.remove() }
}
  .distinctUntilChanged()

fun <T : FleetDelegate> retrievedAssignedFleetById(
  result: Result<List<T>>, assignedAdminId: String
): Result<List<T>> {
  return if (result is Success) {
    val fleetDelegate = result.data retrievedAssignedFleetById assignedAdminId
    Success(fleetDelegate)
  } else Error(result.error)
}

private infix fun <T : FleetDelegate> List<T>.retrievedAssignedFleetById(
  adminId: String
): List<T> = filter { fleetData -> fleetData.assignedAdmin == adminId }