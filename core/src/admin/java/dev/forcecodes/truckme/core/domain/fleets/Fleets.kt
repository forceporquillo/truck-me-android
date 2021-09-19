package dev.forcecodes.truckme.core.domain.fleets

interface Fleets<T> {
  val id: String
  var isActive: Boolean
  var profile: T?
}