package dev.forcecodes.truckme.core.domain.settings

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.auth.FirestoreAuthenticatedUserDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.driverCollection
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPhoneNumberUseCase @Inject constructor(
  private val registeredDriverDataSource: RegisteredDriverDataSource,
  private val authenticatedUserDataSource: FirestoreAuthenticatedUserDataSource,
  private val firestore: FirebaseFirestore,
  @IoDispatcher val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, PhoneNumber?>(ioDispatcher) {

  override fun execute(parameters: String): Flow<Result<PhoneNumber?>> {
    return callbackFlow {
      val phoneNumberListener =
        { snapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
          if (snapshot == null || !snapshot.exists()) {
            if (isDriver) {
              retrieveDriverContactDetails(parameters)
            } else {
              tryOffer(Result.Error(PhoneNumberNotFoundException()))
            }
          } else {
            val phoneNumber = snapshot.toObject<PhoneNumber>()
            tryOffer(Result.Success(phoneNumber))
          }
          Unit
        }

      val phoneNumberListenerSubscription =
        authenticatedUserDataSource.observePhoneNumber(parameters)
          .addSnapshotListener(phoneNumberListener)

      awaitClose { phoneNumberListenerSubscription.remove() }
    }
      .distinctUntilChanged()
  }

  /**
   * In case driver opted not to update his contacts details, we shift the
   * retrieval of driver metadata from AuthenticatedUserDataSource to fleets driver metadata.
   */
  private fun ProducerScope<Result<PhoneNumber>>.retrieveDriverContactDetails(authId: String) {
    launch(ioDispatcher) {
      // check if the driver is already registered or signed in for the first time.
      // retrieve driver id using the authenticated ID from authenticate data source.
      registeredDriverDataSource.getUUIDbyAuthId(authId).collect { result ->
        if (result is Result.Success) {
          val driverDocumentId = result.data
          firestore.driverCollection()
            .document(driverDocumentId)
            .addSnapshotListener { value, _ ->
              if (value?.exists() == true) {
                val driverMetaData = value.toObject<DriverUri>()
                tryOffer(Result.Success(PhoneNumber(driverMetaData?.contact!!)))
              } else {
                tryOffer(Result.Error(PhoneNumberNotFoundException()))
              }
            }
        }
      }
    }
  }
}

class PhoneNumberNotFoundException : Exception()

class PhoneNumberData(
  override var data: PhoneNumber? = null,
  override var exception: Exception? = null,
  override var isSuccess: Boolean = false
) : TaskData<PhoneNumber>

class PhoneNumber @JvmOverloads constructor(val phoneNumber: String = "")

