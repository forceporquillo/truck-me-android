package dev.forcecodes.truckme.core.domain.settings

import dev.forcecodes.truckme.core.data.auth.FirestoreAuthenticatedUserDataSource
import dev.forcecodes.truckme.core.data.driver.DriverDataSource
import dev.forcecodes.truckme.core.data.driver.RegisteredDriverDataSource
import dev.forcecodes.truckme.core.data.driver.UpdatePhoneNumber
import dev.forcecodes.truckme.core.data.driver.UpdatedPassword
import dev.forcecodes.truckme.core.data.fleets.FleetDataSource
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.Result
import dev.forcecodes.truckme.core.util.isDriver
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePhoneNumberUseCase @Inject constructor(
  private val authenticatedUserDataSource: FirestoreAuthenticatedUserDataSource,
  private val driverDataSource: DriverDataSource,
  private val registeredDriverDataSource: RegisteredDriverDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
  @ApplicationScope private val externalScope: CoroutineScope
) : UseCase<Pair<String, PhoneNumber>, PhoneNumberData>(ioDispatcher) {

  override suspend fun execute(parameters: Pair<String, PhoneNumber>): PhoneNumberData {
    val phoneNumberData = PhoneNumberData(data = parameters.second)

    if (isDriver) {
      externalScope.launch(ioDispatcher) {
        registeredDriverDataSource.getUUIDbyAuthId(parameters.first).collect { result ->
          if (result is Result.Success) {
            val documentId = result.data
            val phoneNumber = parameters.second.phoneNumber
            driverDataSource.updatePhoneNumber(UpdatePhoneNumber(documentId, phoneNumber))
          }
        }
      }
    }

    authenticatedUserDataSource.setPhoneNumber(parameters.second, parameters.first)
      .triggerOneShotListener(phoneNumberData)
    return phoneNumberData
  }
}