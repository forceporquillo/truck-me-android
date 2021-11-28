package dev.forcecodes.truckme.core.domain.fleets

import android.net.Uri
import dev.forcecodes.truckme.core.data.fleets.DriverByteArray
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel.DriverUri
import dev.forcecodes.truckme.core.di.IoDispatcher
import dev.forcecodes.truckme.core.mapper.DomainMapperDouble
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class DriverDomainMapper @Inject constructor(
  // context switching of coroutine is unnecessary here.
  @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DomainMapperDouble<DriverByteArray, Uri?, DriverUri> {

  override suspend fun invoke(
    from: DriverByteArray,
    param: Uri?
  ): DriverUri {
    return withContext(dispatcher) {
      from.run {
        DriverUri(
          id = id,
          fullName = fullName,
          email = email,
          password = password,
          contact = contact,
          profile = param.toString(),
          isActive = isActive,
          assignedAdmin = assignedAdminId,
          licenseNumber = licenseNumber,
          licenseExpiration = licenseExpiration,
          restrictions = restrictions
        )
      }
    }
  }
}