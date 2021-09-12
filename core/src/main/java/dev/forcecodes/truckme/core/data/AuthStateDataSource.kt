package dev.forcecodes.truckme.core.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfo
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.data.signin.FirebaseRegisteredUserInfo
import dev.forcecodes.truckme.core.data.signin.FirebaseUserInfo
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.domain.password.Auth
import dev.forcecodes.truckme.core.domain.password.PasswordReset
import dev.forcecodes.truckme.core.util.cancelIfActive
import dev.forcecodes.truckme.core.util.tryOffer
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class AuthBasicInfo(
    val email: String,
    val password: String
)

interface AuthStateDataSource {
    suspend fun requestPasswordReset(email: String): Task<Void>
    fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult>
    fun getAuthenticatedBasicInfo(): Flow<Result<AuthenticatedUserInfoBasic?>>
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
                Timber.e("Auth Id ${auth.uid}")
                Result.Success(FirebaseUserInfo(auth.currentUser))
            }.shareIn(
                scope = externalScope,
                replay = 1,
                started = SharingStarted.WhileSubscribed()
            )

    override suspend fun requestPasswordReset(email: String): Task<Void> {
        return firebaseAuth.sendPasswordResetEmail(email)
    }

    override fun signIn(basicInfo: AuthBasicInfo): Task<AuthResult> {
        return firebaseAuth signInWith basicInfo
    }

    override fun getAuthenticatedBasicInfo() = basicAuthInfo

    private infix fun FirebaseAuth.signInWith(
        basicInfo: AuthBasicInfo
    ) = signInWithEmailAndPassword(
        basicInfo.email,
        basicInfo.password
    )
}

typealias AdminAuthState = ProducerScope<Result<AuthenticatedUserInfo>>

@Singleton
class SignInUseCase @Inject constructor(
    private val authStateDataSource: FirebaseAuthStateDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<AuthBasicInfo, SignIn>(ioDispatcher) {

    override suspend fun execute(parameters: AuthBasicInfo): SignIn {
        val data = SignIn(data = parameters)
        authStateDataSource.signIn(parameters).addOnCompleteListener { task ->
            data.isSuccess = task.isSuccessful
            data.exception = task.exception
        }.await()

        return data
    }

}

data class SignIn(
    override var data: AuthBasicInfo?,
    override var exception: Exception? = null,
    override var isSuccess: Boolean = false
) : Auth<AuthBasicInfo>

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
                send(Result.Error(Exception("FirebaseAuth error")))
            }
        }

        awaitClose { observeUserRegisteredChangesJob.cancelIfActive() }
    }

    override fun execute(
        parameters: Any
    ): Flow<Result<AuthenticatedUserInfoBasic>> = authStateChanges

    private suspend fun AdminAuthState.processUserData(
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

    private suspend fun AdminAuthState.userSignedIn(
        userData: AuthenticatedUserInfoBasic
    ) {
        send(Result.Success(FirebaseRegisteredUserInfo(userData, true)))
    }

    private suspend fun AdminAuthState.userSignedOut(
        userData: AuthenticatedUserInfoBasic
    ) {
        send(Result.Success(FirebaseRegisteredUserInfo(userData, false)))
    }

}