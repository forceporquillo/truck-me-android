package dev.forcecodes.truckme.core.domain.password

import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PasswordResetUseCase @Inject constructor(
    private val authStateDataSource: FirebaseAuthStateDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<String, PasswordReset>(ioDispatcher) {

    override suspend fun execute(parameters: String): PasswordReset {
        val passwordReset = PasswordReset(data = parameters)
        authStateDataSource.requestPasswordReset(parameters)
            .addOnCompleteListener { result ->
                passwordReset.isSuccess = result.isSuccessful
                passwordReset.exception = result.exception
            }.await()
        return passwordReset
    }

}

data class PasswordReset(
    override var data: String? = "",
    override var exception: Exception? = null,
    override var isSuccess: Boolean = false
): Auth<String>

interface Auth<T> {
    var isSuccess: Boolean
    var exception: Exception?
    var data: T?
}