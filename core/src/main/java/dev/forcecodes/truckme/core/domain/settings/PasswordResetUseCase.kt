package dev.forcecodes.truckme.core.domain.settings

import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class PasswordResetUseCase @Inject constructor(
    private val authStateDataSource: FirebaseAuthStateDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<String, PasswordReset>(ioDispatcher) {

    override suspend fun execute(parameters: String): PasswordReset {
        val passwordReset = PasswordReset(data = parameters)
        return authStateDataSource.requestPasswordReset(parameters)
            .triggerOneShotListener(passwordReset)!!
    }
}

data class PasswordReset(
    override var data: String? = "",
    override var exception: Exception? = null,
    override var isSuccess: Boolean = false
) : TaskData<String>


