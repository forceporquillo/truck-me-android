package dev.forcecodes.truckme.core

class NoContactNumberAssociated(
  override val message: String = "No contact number associated."
) : Exception()