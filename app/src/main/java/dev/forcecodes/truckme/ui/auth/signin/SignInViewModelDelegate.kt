package dev.forcecodes.truckme.ui.auth.signin

import android.net.Uri
import dev.forcecodes.truckme.core.data.signin.AuthenticatedUserInfoBasic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SignInViewModelDelegate {
  /**
   * Live updated value of the current firebase user
   */
  val userInfo: StateFlow<AuthenticatedUserInfoBasic?>

  /**
   * Live updated value of the current firebase users image url
   */
  val currentUserImageUri: StateFlow<Uri?>

  val userId: Flow<String?>

  /**
   * Returns the current user ID or null if not available.
   */
  val userIdValue: String?

  /**
   * Live updated value if the current user is signed in
   */
  val isUserSignedIn: StateFlow<Boolean>

  val isUserSignedInValue: Boolean

  /**
   * Emits Events when a sign-in event should be attempted or a dialog shown
   */
  val signInNavigationActions: Flow<AdminAuthState>
}
