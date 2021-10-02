package dev.forcecodes.truckme.core.domain.signin

import dev.forcecodes.truckme.core.data.auth.AuthBasicInfo
import dev.forcecodes.truckme.core.data.auth.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.error
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class DriverAuthInfo(
  val authBasicInfo: AuthBasicInfo?,
  val exception: Exception?,
  val id: String
)

@Singleton
class SignInUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  private val registerDriverDataSource: RegisteredDriverDataSource,
  private val driverDataSource: DriverDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<AuthBasicInfo, SignInResult>(ioDispatcher) {

  override suspend fun execute(parameters: AuthBasicInfo): SignInResult {
    return if (isDriver) {
      getDriverDataOneShot(parameters)
    } else {
      signInRecognizedUsers(parameters)
    }
  }

  /**
   *  Your call if you want to chain some asynchronous work or parallelism.
   *  Just change this method with [AuthDriverDataSource.getDriverCollection].
   *
   *  Note: Based on this current implementation. Spawning multiple coroutines fom
   *  different dispatchers may lead to "JobCancellationException: Job was cancelled."
   *  Either [@see tryOffer] or [async] will throw the JobCancellationException depending
   *  which suspend function emits the deferred result first.
   *
   *  To workaround this, add some try-catch block within the suspend function
   *  and ignore the exception when collecting the exception as result.
   *
   *  This implementation is considered as stable.
   *
   *  @see [dev.forcecodes.truckme.core.util.tryOffer].
   */
  private suspend fun getDriverDataOneShot(
    authBasicInfo: AuthBasicInfo
  ): SignInResult {
    val result = driverDataSource.getDriverCollectionOneShot(authBasicInfo)
    return if (result is Result.Success) {
      observeRegistration(result.data.id, authBasicInfo)
    } else {
      throw result.error
    }
  }

  private suspend fun observeRegistration(
    userId: String,
    authBasicInfo: AuthBasicInfo
  ): SignInResult {
    // Context switching with coroutine IO thread dispatcher.
    return withContext(ioDispatcher) {
      // [Deferred] run in a non-blocking cancellable future.
      val result = async {
        registerDriverDataSource.isDriverRegistered(userId).map { result ->
          if (result is Result.Success) {
            // Check if the driver registration flag is false. This indicates that
            // the driver has recently been added or just signed in for the first time.
            // So, we create the account.
            if (result.data == false /* Kotlin nullability feature */) {
              // triggers only once even if this flow is cold.
              // this is because createUserAccount cancel out when gets invoked.
              val signInResult = SignInResult(data = authBasicInfo)
              authStateDataSource.createUserAccount(authBasicInfo)
                .triggerOneShotListener(signInResult) { task, e ->
                  if (task.isSuccessful) {
                    registerDriverDataSource.updateUid(userId, task.result?.user?.uid)
                  }
                }!!
            } else {
              // Proceed to sign in since auth already recognized the user.
              signInRecognizedUsers(authBasicInfo)
            }
          } else {
            SignInResult(data = authBasicInfo, exception = result.error, isSuccess = false)
          }
        }.first()
      }
      result.await()
    }
  }

  private suspend fun signInRecognizedUsers(
    parameters: AuthBasicInfo
  ): SignInResult = authStateDataSource.signIn(parameters)
    .triggerOneShotListener(SignInResult(data = parameters))!!
}

data class SignInResult(
  val id: String? = "",
  override var data: AuthBasicInfo?,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<AuthBasicInfo>

