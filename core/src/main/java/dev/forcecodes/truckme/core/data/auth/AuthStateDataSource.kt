package dev.forcecodes.truckme.core.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.data.signin.FirebaseUserInfo
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.fcm.FcmTokenUpdater
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject

data class AuthBasicInfo(
  val email: String,
  val password: String
)

interface AuthStateDataSource {
  suspend fun requestPasswordReset(email: String): Task<Void>
  suspend fun updatePassword(password: String): Task<Void>
  suspend fun createUserAccount(authBasicInfo: AuthBasicInfo): Task<AuthResult>
  fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult>
  fun getAuthenticatedBasicInfo(): Flow<Result<AuthenticatedUserInfoBasic?>>
  fun updateProfile(request: UserProfileChangeRequest)
  fun authenticate(credential: AuthCredential): Task<Void>
}

class FirebaseAuthStateDataSource @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
  private val tokenUpdater: FcmTokenUpdater,
  @ApplicationScope externalScope: CoroutineScope
) : AuthStateDataSource {

  private val basicAuthInfo: SharedFlow<Result<AuthenticatedUserInfoBasic?>> =
    callbackFlow {
      val authStateListener: ((FirebaseAuth) -> Unit) = { auth -> tryOffer(auth) }
      firebaseAuth.apply {
        addAuthStateListener(authStateListener)
        awaitClose {
          removeAuthStateListener(authStateListener)
        }
      }
    }
      .map { auth ->
        processAdminAuthState(auth)
      }.shareIn(
        scope = externalScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
      )

  private fun processAdminAuthState(auth: FirebaseAuth): Result<AuthenticatedUserInfoBasic> {
    Timber.d("Received a FirebaseAuth update.")

    if (isDriver) {
      return Result.Success(FirebaseUserInfo(auth.currentUser))
    }

    // if admin we have to append his ID to keep track the generated
    // token Id for delivery item notification.
    auth.currentUser?.let { currentUser ->
      // Save the FCM ID token in firestore
      tokenUpdater.updateTokenForUser(currentUser.uid)
    }

    return Result.Success(FirebaseUserInfo(auth.currentUser))
  }

  override suspend fun updatePassword(password: String): Task<Void> {
    return firebaseAuth.currentUser!!.updatePassword(password)
  }

  override suspend fun requestPasswordReset(email: String): Task<Void> {
    return firebaseAuth.sendPasswordResetEmail(email)
  }

  override fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult> {
    return firebaseAuth signInWith basicInfo
  }

  override suspend fun createUserAccount(authBasicInfo: AuthBasicInfo): Task<AuthResult> {
    val (email, password) = authBasicInfo
    return firebaseAuth.createUserWithEmailAndPassword(email, password)
  }

  override fun updateProfile(request: UserProfileChangeRequest) {
    firebaseAuth.currentUser!!.updateProfile(request)
  }

  override fun authenticate(credential: AuthCredential): Task<Void> {
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
