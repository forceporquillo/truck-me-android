package dev.forcecodes.truckme.core.fcm

import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.MainDispatcher
import dev.forcecodes.truckme.core.util.storeAdminToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import timber.log.Timber
import javax.inject.Inject

class FcmTokenUpdater @Inject constructor(
  @ApplicationScope private val externalScope: CoroutineScope,
  @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
  @ApplicationContext private val context: Context,
  private val firestore: FirebaseFirestore
) {

  companion object {
    private const val LAST_VISIT_KEY = "lastVisit"
    private const val TOKEN_ID_KEY = "tokenId"
  }

  fun updateTokenForUser(userId: String) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

      context.storeAdminToken(token)

      val tokenInfo = mapOf(
        LAST_VISIT_KEY to FieldValue.serverTimestamp(),
        TOKEN_ID_KEY to token
      )

      externalScope.launch(mainDispatcher) {
        firestore.collection("admin")
          .document(userId)
          .set(tokenInfo, SetOptions.merge())
          .addOnCompleteListener {
            if (it.isSuccessful) {
              Timber.d("FCM ID token successfully uploaded for user $userId\"")
            } else {
              Timber.e("FCM ID token: Error uploading for user $userId")
            }
          }
      }
    }
  }
}


data class PushMessage(
  val to: String,
  val data: MessageData
)

data class MessageData(
  val title: String,
  val items: String,
  val jobId: String,
  val driver: String
)