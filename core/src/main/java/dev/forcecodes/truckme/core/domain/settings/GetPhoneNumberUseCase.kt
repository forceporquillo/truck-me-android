package dev.forcecodes.truckme.core.domain.settings

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import dev.forcecodes.truckme.core.data.FirestoreAuthenticatedUserDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.FlowUseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.TaskData
import dev.forcecodes.truckme.core.util.tryOffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPhoneNumberUseCase @Inject constructor(
    private val authenticatedUserDataSource: FirestoreAuthenticatedUserDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlowUseCase<String, PhoneNumber?>(ioDispatcher) {

    override fun execute(parameters: String): Flow<Result<PhoneNumber?>> {
        return callbackFlow {
            val phoneNumberListener =
                { snapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                    if (snapshot == null || !snapshot.exists()) {
                        tryOffer(Result.Error(PhoneNumberNotFoundException()))
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
}

class PhoneNumberNotFoundException : Exception()

class PhoneNumberData(
    override var data: PhoneNumber? = null,
    override var exception: Exception? = null,
    override var isSuccess: Boolean = false
) : TaskData<PhoneNumber>

class PhoneNumber @JvmOverloads constructor(val phoneNumber: String = "")

