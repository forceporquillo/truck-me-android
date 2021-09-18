package dev.forcecodes.truckme.ui.auth.signin

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.truckme.base.UiActionEvent
import dev.forcecodes.truckme.core.data.AuthBasicInfo
import dev.forcecodes.truckme.core.data.ObserveAuthStateUseCase
import dev.forcecodes.truckme.core.data.SignInUseCase
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.WhileViewSubscribed
import dev.forcecodes.truckme.ui.auth.BaseAuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FirebaseSignInViewModel @Inject constructor(
    signInViewModelDelegate: SignInViewModelDelegate,
    private val signInUseCase: SignInUseCase,
) : BaseAuthViewModel<SignInNavActions>(), SignInViewModelDelegate by signInViewModelDelegate {

    val signInNavActions = mUiEvents.receiveAsFlow()

    fun signIn() {
        submitAndSetLoading()
        viewModelScope.launch {
            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                return@launch
            }

            val useCase = signInUseCase(AuthBasicInfo(email!!, password!!))
            fetchResult(useCase) { _, _ -> /* do nothing */ }
        }
    }

    fun resetPassword() {
        sendUiEvent(SignInNavActions.ResetPasswordAction)
    }
}

sealed class SignInNavActions : UiActionEvent {
    object ResetPasswordAction : SignInNavActions()
    object MainDashboardAction : SignInNavActions()
}

sealed class AdminAuthState {
    object SignedOut : AdminAuthState()
    object SignedIn : AdminAuthState()
}

class FirebaseSignInViewModelDelegate @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    @ApplicationScope private val applicationScope: CoroutineScope
) : SignInViewModelDelegate {

    private val _signInNavigationActions = Channel<AdminAuthState>(Channel.CONFLATED)
    override val signInNavigationActions = _signInNavigationActions.receiveAsFlow()

    private val currentAdminUser: Flow<Result<AuthenticatedUserInfoBasic>> =
        observeAuthStateUseCase(Any()).map { result ->
            if (result is Result.Error) {
                Timber.e(result.exception.message.toString())
            }
            result
        }

    override val userInfo: StateFlow<AuthenticatedUserInfoBasic?> = currentAdminUser.map {
        (it as? Result.Success)?.data
    }.stateIn(applicationScope, WhileViewSubscribed, null)

    override val currentUserImageUri: StateFlow<Uri?> = userInfo.map {
        it?.getPhotoUrl()
    }.stateIn(applicationScope, WhileViewSubscribed, null)

    override val isUserSignedIn: StateFlow<Boolean> = userInfo.map {
        it?.isSignedIn() ?: false
    }.stateIn(applicationScope, WhileViewSubscribed, false)

    override val userId: Flow<String?>
        get() = userInfo.mapLatest { it?.getUid() }
            .stateIn(applicationScope, WhileViewSubscribed, null)

    override val userIdValue: String?
        get() = userInfo.value?.getUid()

    override val isUserSignedInValue: Boolean
        get() = isUserSignedIn.value

    init {
        applicationScope.launch {
            userInfo.debounce(500L).collectLatest {
                if (it?.isSignedIn() == true) {
                    _signInNavigationActions.send(AdminAuthState.SignedIn)
                }
            }
        }
    }
}


