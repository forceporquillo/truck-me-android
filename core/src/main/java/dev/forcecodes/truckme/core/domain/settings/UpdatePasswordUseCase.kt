package dev.forcecodes.truckme.core.domain.settings

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import dev.forcecodes.truckme.core.data.auth.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.driver.UpdatedPassword
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.core.util.notNullData
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePasswordUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  private val registeredDriverDataSource: RegisteredDriverDataSource,
  private val driverDataSource: DriverDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
  @ApplicationScope private val externalScope: CoroutineScope
) : UseCase<UserPasswordCredentials, PasswordUpdate>(ioDispatcher) {

  override suspend fun execute(parameters: UserPasswordCredentials): PasswordUpdate {
    val (email, newPasswords, oldPassword) = parameters
    val passwordUpdate = PasswordUpdate(data = newPasswords)
    reauthenticate(email, oldPassword).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        updatePassword(parameters, passwordUpdate)
      } else {
        Timber.e(task.exception?.message.toString())
      }
    }.await()
    return passwordUpdate
  }

  private fun reauthenticate(
    email: String,
    oldPassword: String
  ): Task<Void> {
    val authCredential = EmailAuthProvider.getCredential(email, oldPassword)
    return authStateDataSource.reauthenticate(authCredential)
  }

  private fun updatePassword(
    userPasswordCredentials: UserPasswordCredentials,
    passwordUpdate: PasswordUpdate
  ) {
    externalScope.launch(ioDispatcher) {
      if (isDriver) {
        val update = async {
          registeredDriverDataSource.getUUIDbyAuthId(userPasswordCredentials.authId).map { result ->
            val updatedPassword = UpdatedPassword(result.notNullData, userPasswordCredentials.newPassword)
            driverDataSource.updateDriverPassword(updatedPassword)
          }.first()
        }
        update.await()
      }
      authStateDataSource.updatePassword(userPasswordCredentials.newPassword)
        .triggerOneShotListener(passwordUpdate)
    }
  }
}

data class UserPasswordCredentials(
  val email: String,
  val newPassword: String,
  val oldPassword: String,
  val authId: String? = null,
)

data class PasswordUpdate(
  override var data: String? = "",
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<String>

