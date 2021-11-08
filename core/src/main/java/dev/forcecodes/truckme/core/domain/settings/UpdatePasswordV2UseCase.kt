package dev.forcecodes.truckme.core.domain.settings

import com.google.firebase.auth.FirebaseAuth
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.UpdatedPassword
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.di.SecondaryFirebaseAuth
import dev.forcecodes.truckme.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class UpdatedPasswordV2(
  val documentId: String,
  val email: String,
  val oldPassword: String,
  val newPassword: String
)

@Singleton
class UpdatePasswordV2UseCase @Inject constructor(
  @SecondaryFirebaseAuth private val firebaseAuth: FirebaseAuth,
  private val driverDataSource: DriverDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
  @ApplicationScope private val externalScope: CoroutineScope
) : UseCase<UpdatedPasswordV2, Boolean>(ioDispatcher) {

  override suspend fun execute(parameters: UpdatedPasswordV2): Boolean {
    val (documentId, email, oldPassword, newPassword) = parameters

    var success = false

    firebaseAuth.signInWithEmailAndPassword(email, oldPassword).addOnCompleteListener {
      if (it.isSuccessful) {
        firebaseAuth.currentUser?.updatePassword(newPassword)
        updateDriverPassword(UpdatedPassword(documentId, newPassword))
        success = true
      } else {
        success = false
        Timber.e(it.exception)
      }
    }.await()
    return success
  }

  private fun updateDriverPassword(updatedPassword: UpdatedPassword) {
    externalScope.launch(ioDispatcher) {
      driverDataSource.updateDriverPassword(updatedPassword)
    }
  }
}