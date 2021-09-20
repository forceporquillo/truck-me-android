package dev.forcecodes.truckme.core.domain.signin

import com.google.firebase.firestore.BuildConfig
import dev.forcecodes.truckme.core.data.AuthBasicInfo
import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignInUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<AuthBasicInfo, SignInResult>(ioDispatcher) {

  override suspend fun execute(parameters: AuthBasicInfo): SignInResult {
    if (BuildConfig.FLAVOR == "driver") {

    }

    val data = SignInResult(data = parameters)
    return authStateDataSource.signIn(parameters)
      .triggerOneShotListener(data)!!
  }

  private fun authenticateNewlyCreatedDriver() {
  }
}

data class SignInResult(
  override var data: AuthBasicInfo?,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<AuthBasicInfo>

