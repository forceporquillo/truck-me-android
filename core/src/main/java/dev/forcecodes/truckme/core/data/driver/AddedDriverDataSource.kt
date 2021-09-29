package dev.forcecodes.truckme.core.data.driver

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.auth.AuthBasicInfo
import dev.forcecodes.truckme.core.data.fleets.EmptyFleetsException
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.domain.signin.DriverAuthInfo
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.driverCollection
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UpdatedPassword(val documentId: String, val newPassword: String)

interface DriverDataSource {
  suspend fun updateDriverPassword(updatedPassword: UpdatedPassword)
  suspend fun getDriverCollection(authInfo: AuthBasicInfo): Flow<Result<DriverAuthInfo>>
  suspend fun getDriverCollectionOneShot(authInfo: AuthBasicInfo): Result<DriverAuthInfo>
}

@Singleton
class AddedDriverDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : DriverDataSource {

  override suspend fun updateDriverPassword(updatedPassword: UpdatedPassword) {
    val (uid, newPassword) = updatedPassword
    firestore.driverCollection()
      .document(uid)
      .update(mapOf("password" to newPassword))
  }

  // async hot flows backed by snapshot listener
  override suspend fun getDriverCollection(
    authInfo: AuthBasicInfo
  ): Flow<Result<DriverAuthInfo>> {
    return callbackFlow {
      val driverCollectionListener = firestore.driverCollection()
        .addSnapshotListener { snapshot, error ->
          if (snapshot?.isEmpty == false) {
            tryOffer(checkDriverExistence(snapshot, authInfo))
          } else {
            tryOffer(Result.Error(EmptyFleetsException(error)))
          }
          Unit
        }
      awaitClose { driverCollectionListener.remove() }
    }.distinctUntilChanged()
  }

  // one shot callback listener
  override suspend fun getDriverCollectionOneShot(
    authInfo: AuthBasicInfo
  ): Result<DriverAuthInfo> {
    var result: Result<DriverAuthInfo> = Result.Loading
    firestore.driverCollection().get()
      .addOnCompleteListener { task ->
        result = if (task.isSuccessful) {
          val snapshot = task.result
          if (snapshot?.isEmpty == false) {
            checkDriverExistence(snapshot, authInfo)
          } else {
            Result.Error(EmptyFleetsException(task.exception))
          }
        } else {
          Result.Error(Exception(task.exception))
        }
      }.await()
    return result
  }

  private fun checkDriverExistence(
    querySnapshots: QuerySnapshot,
    authInfo: AuthBasicInfo
  ): Result<DriverAuthInfo> {
    var result: Result<DriverAuthInfo> = Result.Loading
    for (driver in querySnapshots) {
      val driverSnapshot = driver.toObject<DriverUri>()
      if (isDriverExist(driverSnapshot, authInfo)) {
        if (isValidCredentials(driverSnapshot, authInfo)) {
          result = Result.Success(DriverAuthInfo(authInfo, null, driverSnapshot.id))
          break
        } else {
          result = Result.Error(InvalidDriverCredentialsException())
        }
      } else {
        if (isInvalidCredentials(driverSnapshot, authInfo)) {
          result = Result.Error(InvalidDriverCredentialsException())
          break
        }
      }
    }
    val nonExistentDriver = querySnapshots.none { snapshots ->
      snapshots.toObject<DriverUri>().email == authInfo.email
    }
    if (nonExistentDriver) {
      result = Result.Error(DriverNotRegisteredException())
    }
    return result
  }

  private fun isDriverExist(
    driverSnapshot: DriverUri,
    authInfo: AuthBasicInfo
  ): Boolean {
    return driverSnapshot.email == authInfo.email
  }

  private fun isValidCredentials(
    driverSnapshot: DriverUri,
    authInfo: AuthBasicInfo
  ): Boolean {
    return driverSnapshot.email == authInfo.email
      && driverSnapshot.password == authInfo.password
  }

  private fun isInvalidCredentials(
    driverSnapshot: DriverUri,
    authInfo: AuthBasicInfo
  ): Boolean {
    return driverSnapshot.email == authInfo.email
      && driverSnapshot.password != authInfo.password
  }
}



