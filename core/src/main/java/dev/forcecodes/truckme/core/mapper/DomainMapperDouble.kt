package dev.forcecodes.truckme.core.mapper

interface DomainMapperDouble<in T1, T2, R> {
  suspend operator fun invoke(from: T1, param: T2): R
}