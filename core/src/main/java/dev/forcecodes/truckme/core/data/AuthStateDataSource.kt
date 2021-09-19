package dev.forcecodes.truckme.core.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfo
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.data.signin.FirebaseRegisteredUserInfo
import dev.forcecodes.truckme.core.data.signin.FirebaseUserInfo
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.cancelIfActive
import dev.forcecodes.truckme.core.util.error
import dev.forcecodes.truckme.core.util.phoneNumberDocument
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import dev.forcecodes.truckme.core.util.tryOffer
import dev.forcecodes.truckme.core.util.updatePhoneNumberDocument
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class AuthBasicInfo(
  val email: String,
  val password: String
)

interface AuthStateDataSource {
  suspend fun requestPasswordReset(email: String): Task<Void>
  suspend fun updatePassword(password: String): Task<Void>
  fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult>
  fun getAuthenticatedBasicInfo(): Flow<Result<AuthenticatedUserInfoBasic?>>
  fun updateProfile(request: UserProfileChangeRequest)
  fun reauthenticate(credential: AuthCredential): Task<Void>
}

class FirebaseAuthStateDataSource @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
  @ApplicationScope externalScope: CoroutineScope
) : AuthStateDataSource {

  private val basicAuthInfo: SharedFlow<Result<AuthenticatedUserInfoBasic?>> =
    callbackFlow {
      val authStateListener: ((FirebaseAuth) -> Unit) = { auth ->
        tryOffer(auth)
      }
      firebaseAuth.apply {
        addAuthStateListener(authStateListener)
        awaitClose {
          removeAuthStateListener(authStateListener)
        }
      }
    }
      .map { auth ->
        Result.Success(FirebaseUserInfo(auth.currentUser))
      }.shareIn(
        scope = externalScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
      )

  override suspend fun updatePassword(password: String): Task<Void> {
    return firebaseAuth.currentUser!!.updatePassword(password)
  }

  override suspend fun requestPasswordReset(email: String): Task<Void> {
    return firebaseAuth.sendPasswordResetEmail(email)
  }

  override fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult> {
    return firebaseAuth signInWith basicInfo
  }

  override fun updateProfile(request: UserProfileChangeRequest) {
    firebaseAuth.currentUser!!.updateProfile(request)
  }

  override fun reauthenticate(credential: AuthCredential): Task<Void> {
    return firebaseAuth.currentUser!!.reauthenticate(credential)
  }

  override fun getAuthenticatedBasicInfo() = basicAuthInfo

  private infix fun FirebaseAuth.signInWith(
    basicInfo: AuthBasicInfo
  ) = signInWithEmailAndPassword(
    basicInfo.email,
    basicInfo.password
  )
}

typealias UserAuthState = ProducerScope<Result<AuthenticatedUserInfo>>

@Singleton
class SignInUseCase @Inject constructor(
  private val authStateDataSource: FirebaseAuthStateDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<AuthBasicInfo, SignInResult>(ioDispatcher) {

  override suspend fun execute(parameters: AuthBasicInfo): SignInResult {
    val data = SignInResult(data = parameters)
    return authStateDataSource.signIn(parameters)
      .triggerOneShotListener(data)!!
  }
}

data class SignInResult(
  override var data: AuthBasicInfo?,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<AuthBasicInfo>

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

      Timber.e("Result.Success -> ${userResult is Result.Success}")

      if (userResult is Result.Success) {
        if (userResult.data != null) {
          processUserData(userResult.data)
        } else {
          Timber.e("isRegistered null")
          send(Result.Success(FirebaseRegisteredUserInfo(null, null)))
        }
      } else {
        send(Result.Error(userResult.error))
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
      send(Result.Success(FirebaseRegisteredUserInfo(userData, false)))
    }
  }

  private suspend fun UserAuthState.userSignedIn(
    userData: AuthenticatedUserInfoBasic
  ) {
    send(Result.Success(FirebaseRegisteredUserInfo(userData, false)))
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
  ) = Result.Success(FirebaseRegisteredUserInfo(userData, isAdmin))
}

class FirestoreAuthenticatedUserDataSource @Inject constructor(
  val firestore: FirebaseFirestore
) : AuthenticatedUserDataSource {

  override fun observePhoneNumber(userId: String) = firestore.phoneNumberDocument(userId)

  override fun setPhoneNumber(
    phoneNumber: PhoneNumber,
    userId: String
  ) =
    firestore.updatePhoneNumberDocument(userId, phoneNumber)
}

interface AuthenticatedUserDataSource {
  fun setPhoneNumber(
    phoneNumber: PhoneNumber,
    userId: String
  ): Task<Void>

  fun observePhoneNumber(userId: String): DocumentReference
}