package dev.forcecodes.truckme.core.domain.signin

import dev.forcecodes.truckme.core.data.FirebaseAuthStateDataSource
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfo
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.data.signin.FirebaseRegisteredUserInfo
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.Result.Error
import dev.forcecodes.truckme.core.util.Result.Success
import dev.forcecodes.truckme.core.util.cancelIfActive
import dev.forcecodes.truckme.core.util.error
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

typealias UserAuthState = ProducerScope<Result<AuthenticatedUserInfo>>

@Singleton
class ObserveAuthStateUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  @ApplicationScope private val externalScope: CoroutineScope,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<Any, AuthenticatedUserInfoBasic>(ioDispatcher) {

  private var observeUserRegisteredChangesJob: Job? = null

  private val authStateChanges = callbackFlow<Result<AuthenticatedUserInfoBasic>> {
    authStateDataSource.getAuthenticatedBasicInfo().collect { userResult ->
      observeUserRegisteredChangesJob.cancelIfActive()

      Timber.e("Result.Success -> ${userResult is Success}")

      if (userResult is Success) {
        if (userResult.data != null) {
          Timber.e(userResult.data.getUid())
          processUserData(userResult.data)
        } else {
          Timber.e("isRegistered null")
          send(Success(FirebaseRegisteredUserInfo(null, null)))
        }
      } else {
        send(Error(userResult.error))
      }
    }

    awaitClose { observeUserRegisteredChangesJob.cancelIfActive() }
  }
    .shareIn(externalScope, SharingStarted.WhileSubscribed())

  override fun execute(
    parameters: Any
  ): Flow<Result<AuthenticatedUserInfoBasic>> = authStateChanges

  private suspend fun UserAuthState.processUserData(
    userData: AuthenticatedUserInfoBasic
  ) {
    if (!userData.isSignedIn()) {
      userSignedOut(userData)
    } else if (userData.getUid() != null) {
      userSignedIn(userData)
    } else {
      send(Success(FirebaseRegisteredUserInfo(userData, false)))
    }
  }

  private suspend fun UserAuthState.userSignedIn(
    userData: AuthenticatedUserInfoBasic
  ) {
    send(Success(FirebaseRegisteredUserInfo(userData, false)))
  }

  private suspend fun UserAuthState.userSignedOut(
    userData: AuthenticatedUserInfoBasic
  ) {
    send(FirebaseRegisteredUserInfoResult(userData, false))
  }

  @Suppress("FunctionName")
  private fun FirebaseRegisteredUserInfoResult(
    userData: AuthenticatedUserInfoBasic,
    isAdmin: Boolean
  ) = Success(FirebaseRegisteredUserInfo(userData, isAdmin))
}