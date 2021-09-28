package dev.forcecodes.truckme.core.domain.settings

import dev.forcecodes.truckme.core.data.auth.FirestoreAuthenticatedUserDataSource
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.domain.UseCase
import dev.forcecodes.truckme.core.util.triggerOneShotListener
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePhoneNumberUseCase @Inject constructor(
  private val authenticatedUserDataSource: FirestoreAuthenticatedUserDataSource,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<Pair<String, PhoneNumber>, PhoneNumberData>(ioDispatcher) {

  override suspend fun execute(parameters: Pair<String, PhoneNumber>): PhoneNumberData {
    val phoneNumberData = PhoneNumberData(data = parameters.second)
    authenticatedUserDataSource.setPhoneNumber(parameters.second, parameters.first)
      .triggerOneShotListener(phoneNumberData)
    return phoneNumberData
  }
}