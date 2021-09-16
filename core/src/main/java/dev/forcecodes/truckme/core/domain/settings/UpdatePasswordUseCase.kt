package dev.forcecodes.truckme.core.domain.settings

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePasswordUseCase @Inject constructor(
    private val authStateDataSource: FirebaseAuthStateDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val externalScope: CoroutineScope
) : UseCase<CurrentUserPassword, PasswordUpdate>(ioDispatcher) {

    override suspend fun execute(parameters: CurrentUserPassword): PasswordUpdate {
        val (email, newPasswords, oldPassword) = parameters
        val passwordUpdate = PasswordUpdate(data = newPasswords)
        reauthenticate(email, oldPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.e("User authenticated")
                updatePassword(newPasswords, passwordUpdate)
            } else {
                Timber.e(task.exception?.message.toString())
            }
        }.await()
        return passwordUpdate
    }

    private fun reauthenticate(email: String, oldPassword: String): Task<Void> {
        val authCredential = EmailAuthProvider.getCredential(email, oldPassword)

        return authStateDataSource.reauthenticate(authCredential)
    }

    private fun updatePassword(newPassword: String, passwordUpdate: PasswordUpdate) {
        externalScope.launch(ioDispatcher) {
            authStateDataSource.updatePassword(newPassword)
                .triggerOneShotListener(passwordUpdate)
        }
    }
}

data class CurrentUserPassword(
    val email: String,
    val newPassword: String,
    val oldPassword: String
)

data class PasswordUpdate(
    override var data: String? = "",
    override var exception: Exception? = null,
    override var isSuccess: Boolean = false
) : TaskData<String>

