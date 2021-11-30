package dev.forcecodes.truckme.ui.fleet

import kotlinx.coroutines.flow.Flow

internal fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineFleetInfo(
  flow1: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(
  kotlinx.coroutines.flow.combine(flow1, flow2, flow3, flow4, ::FleetTuple),
  kotlinx.coroutines.flow.combine(flow5, flow6, flow7, flow8, ::FleetTuple)
) { tuple1, tuple2 ->
  transform(
    tuple1.t1, tuple1.t2, tuple1.t3, tuple1.t4,
    tuple2.t1, tuple2.t2, tuple2.t3, tuple2.t4,
  )
}
