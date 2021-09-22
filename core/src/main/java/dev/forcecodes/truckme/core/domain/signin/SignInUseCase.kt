package dev.forcecodes.truckme.core.domain.signin

import dev.forcecodes.truckme.core.data.AuthBasicInfo
import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.error
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

  companion object {
    private const val BUILD_VARIANT = dev.forcecodes.truckme.core.BuildConfig.FLAVOR
  }

  override suspend fun execute(parameters: AuthBasicInfo): SignInResult {
    return if (BUILD_VARIANT == "driver") {
      getDriverDataOneShot(parameters)
    } else {
      signInRecognizedUsers(parameters)
    }
  }

  private suspend fun getDriverDataOneShot(
    authBasicInfo: AuthBasicInfo
  ): SignInResult {
    // your call if you want to chain some asynchronous work / parallelism
    // just change this method with [driverDataSource.getDriverCollection].

    // Note that when working with cold flows. This may lead to
    // "JobCancellationException: Job was cancelled." This is because [tryOffer]
    // method in [getDriverCollection] is synchronous which eventually
    // throws the exception once this coroutine scope gets cancelled.
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
            if (result.data == false) {
              // triggers only once even if this flow is cold.
              // this is because createUserAccount cancel out when gets invoked.
              authStateDataSource.createUserAccount(authBasicInfo)
                .triggerOneShotListener(SignInResult(data = authBasicInfo))!!
            } else {
              // Proceed to sign in since auth already recognized this user.
              signInRecognizedUsers(authBasicInfo)
            }
          } else {
            SignInResult(data = authBasicInfo, result.error, false)
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
  override var data: AuthBasicInfo?,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<AuthBasicInfo>

