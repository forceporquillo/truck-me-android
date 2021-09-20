package dev.forcecodes.truckme.core.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.BuildConfig
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