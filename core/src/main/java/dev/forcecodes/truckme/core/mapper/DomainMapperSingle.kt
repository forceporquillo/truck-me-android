package dev.forcecodes.truckme.core.mapper

interface DomainMapperSingle<in T1, R> {
  suspend operator fun invoke(
    from: T1
  ): R
}