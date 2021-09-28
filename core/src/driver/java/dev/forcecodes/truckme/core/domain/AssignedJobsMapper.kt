package dev.forcecodes.truckme.core.domain

import dev.forcecodes.truckme.core.data.ActiveJobItems
import dev.forcecodes.truckme.core.mapper.DomainMapperSingle
import dev.forcecodes.truckme.core.model.DeliveryInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignedJobsMapper @Inject constructor() :
  DomainMapperSingle<DeliveryInfo, ActiveJobItems> {
  override suspend fun invoke(from: DeliveryInfo): ActiveJobItems {
    return from.run {
      ActiveJobItems(
        id = id,
        title = title,
        destination = destination?.address ?: destination?.title ?: "",
        items = items
      )
    }
  }
}