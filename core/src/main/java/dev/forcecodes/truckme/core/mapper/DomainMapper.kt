package dev.forcecodes.truckme.core.mapper

interface DomainMapper<in T1, T2, R> {
  suspend operator fun invoke(
    from: T1,
    param: T2
  ): R
}

interface DomainMapperSingle<in T1, R> {
  suspend operator fun invoke(
    from: T1
  ): R
}