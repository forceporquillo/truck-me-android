package dev.forcecodes.truckme.core.util

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.domain.settings.ProfileData
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import kotlinx.coroutines.tasks.await

private const val PROFILE = "profile.jpg"
private const val REFERENCE = BuildConfig.FLAVOR

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
