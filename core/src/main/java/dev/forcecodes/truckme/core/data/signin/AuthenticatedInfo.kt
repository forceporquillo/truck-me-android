package dev.forcecodes.truckme.core.data.signin

import android.net.Uri
import com.google.firebase.auth.UserInfo

/**
 * Basic user info.
 */
interface AuthenticatedUserInfoBasic {

  fun isSignedIn(): Boolean

  fun getEmail(): String?

  fun getProviderData(): MutableList<out UserInfo>?

  fun getLastSignInTimestamp(): Long?

  fun getCreationTimestamp(): Long?

  fun isAnonymous(): Boolean?

  fun getPhoneNumber(): String?

  fun getUid(): String?

  fun isEmailVerified(): Boolean?

  fun getDisplayName(): String?

  fun getPhotoUrl(): Uri?

  fun getProviderId(): String?
}

interface AuthenticatedUserInfoRegistered {

  fun isAdmin(): Boolean?
}

/**
 * Interface to decouple the user info from Firebase.
 *
 * @see [FirebaseRegisteredUserInfo]
 */
interface AuthenticatedUserInfo : AuthenticatedUserInfoBasic, AuthenticatedUserInfoRegistered
