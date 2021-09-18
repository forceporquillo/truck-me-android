package dev.forcecodes.truckme.core.util

import android.net.Uri
import androidx.annotation.StringDef
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.data.fleets.EmptyFleetsException
import dev.forcecodes.truckme.core.domain.fleets.FleetProfileData
import dev.forcecodes.truckme.core.domain.fleets.Fleets
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

internal const val PROFILE = "profile.jpg"
internal const val REFERENCE = BuildConfig.FLAVOR

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

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@StringDef(value = [VEHICLE, DRIVER])
annotation class FleetDelegate

interface TaskData<T> {
    var isSuccess: Boolean
    var exception: Exception?
    var data: T?
}

suspend inline fun <T : TaskData<U>, U, V> Task<V>.triggerOneShotListener(
    data: T? = null, noinline block: ((t: Task<V>, e: Exception?) -> Unit)? = null
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

inline fun <reified T: Any> CollectionReference.fleetSnapshots(): Flow<Result<List<T>>> =
    callbackFlow {
        val fleetSubscriptionListener = this@fleetSnapshots
            .addSnapshotListener { snapshot: QuerySnapshot?, _: FirebaseFirestoreException? ->
                if (snapshot?.isEmpty == false) {
                    val fleetList = mutableListOf<T>()
                    snapshot.forEach { data ->
                        val fleetData = data.toObject<T>()
                        fleetList.add(fleetData)
                    }
                    tryOffer(Result.Success(fleetList))
                } else {
                    tryOffer(Result.Error(EmptyFleetsException()))
                }
                Unit
            }
        awaitClose { fleetSubscriptionListener.remove() }
    }
        .distinctUntilChanged()