package dev.forcecodes.truckme.core.domain.statistics

import androidx.annotation.StringDef

const val DAILY = "DAILY"
const val DRIVER = "driver"

@Target(
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.TYPE_PARAMETER,
  AnnotationTarget.TYPE,
  AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.SOURCE)
@StringDef(value = [DAILY, DRIVER])
annotation class StatisticsSortType
