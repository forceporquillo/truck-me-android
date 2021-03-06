package dev.forcecodes.truckme.core.data.driver

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Error
import dev.forcecodes.truckme.core.util.Result.Success
import dev.forcecodes.truckme.core.util.fleetDrivers
import dev.forcecodes.truckme.core.util.fleetRegDocument
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

interface RegisteredDriverDataSource {
  suspend fun isDriverRegisteredOnShot(userId: String): Result<Boolean?>
  fun isDriverRegistered(userId: String): Flow<Result<Boolean?>>
  fun register(userId: String, isRegistered: Boolean = false)
  fun updateUid(userId: String, authId: String?)
  fun getUUIDbyAuthId(authId: String?): Flow<Result<String>>
}

class RegisteredDriverDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : RegisteredDriverDataSource {

  companion object {
    private const val REGISTERED_KEY = "registered"
    private const val AUTH_ID = "auth_id"
  }

  override suspend fun isDriverRegisteredOnShot(userId: String): Result<Boolean?> {
    var result: Result<Boolean?> = Result.Loading
    firestore.fleetRegDocument(userId)
      .get()
      .addOnCompleteListener { task ->
        result = if (task.isSuccessful) {
          val document = task.result
          val isRegistered: Boolean? = document?.get(REGISTERED_KEY) as? Boolean
          Success(isRegistered)
        } else {
          Error(task.exception ?: Exception(task.exception?.message))
        }
      }.await()
    return result
  }

  override fun isDriverRegistered(userId: String): Flow<Result<Boolean?>> {
    return callbackFlow {
      val registeredChangedListener =
        { snapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
          Timber.e(e)
          if (snapshot == null || !snapshot.exists()) {
            tryOffer(Error(DriverNotRegisteredException()))
          } else {
            val isRegistered: Boolean? = snapshot.get(REGISTERED_KEY) as? Boolean
            if (isRegistered == false) {
              register(userId, true)
            }
            tryOffer(Success(isRegistered))
          }
          Unit
        }

      val registeredChangeListenerSubscription = firestore
        .fleetRegDocument(userId)
        .addSnapshotListener(registeredChangedListener)

      awaitClose { registeredChangeListenerSubscription.remove() }
    }
      .distinctUntilChanged()
  }

  override fun updateUid(userId: String, authId: String?) {
    firestore.fleetRegDocument(userId)
      .update(mapOf(AUTH_ID to authId))
  }

  override fun register(userId: String, isRegistered: Boolean) {
    firestore.fleetRegDocument(userId)
      .set(mapOf(REGISTERED_KEY to isRegistered, AUTH_ID to ""))
  }

  override fun getUUIDbyAuthId(authId: String?): Flow<Result<String>> {
    return callbackFlow {
      val authIdListener = firestore.fleetDrivers().addSnapshotListener { value, _ ->
        if (value?.isEmpty == false) {
          value.forEach { querySnapshot ->
            val id = querySnapshot.get(AUTH_ID) as String
            if (authId == id) {
              Timber.e(id)
              tryOffer(Success(querySnapshot.id))
            }
          }
        } else {
          tryOffer(Error(Exception()))
        }
      }
      awaitClose { authIdListener.remove() }
    }
      .distinctUntilChanged()
  }
}