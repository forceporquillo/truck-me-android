package dev.forcecodes.truckme.core.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dev.forcecodes.truckme.core.domain.settings.PhoneNumber
import dev.forcecodes.truckme.core.util.phoneNumberDocument
import dev.forcecodes.truckme.core.util.updatePhoneNumberDocument
import javax.inject.Inject

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