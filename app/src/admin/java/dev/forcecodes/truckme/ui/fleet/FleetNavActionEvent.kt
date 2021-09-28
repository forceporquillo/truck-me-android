package dev.forcecodes.truckme.ui.fleet

import dev.forcecodes.truckme.base.UiActionEvent

sealed class FleetNavActionEvent : UiActionEvent {
  object AddDriver : FleetNavActionEvent()
  object AddVehicle : FleetNavActionEvent()
}