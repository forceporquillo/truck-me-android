package dev.forcecodes.truckme.ui.fleet

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.core.domain.fleets.FleetStateUpdateMetadata
import dev.forcecodes.truckme.core.util.then
import dev.forcecodes.truckme.databinding.FleetItemBinding
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.setActiveStateIndicatorColor
import dev.forcecodes.truckme.extensions.setNotAvailable
import timber.log.Timber
import timber.log.Timber.Forest

abstract class BaseFleetAdapter<T : FleetUiModel>(
  private val emptyState: (Boolean) -> Unit,
  @StringRes private val adapterTitle: Int
) : ListAdapter<T, BaseFleetAdapter.FleetViewHolder>(
  object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(
      oldItem: T,
      newItem: T
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
      oldItem: T,
      newItem: T
    ): Boolean {
      if (oldItem is FleetUiModel.DriverUri) {
        return oldItem == newItem
      }

      if (oldItem is FleetUiModel.VehicleUri) {
        return oldItem == newItem
      }

      throw IllegalStateException()
    }
  }) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): FleetViewHolder {
    val binding = FleetItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent, false
    ).apply {
      fleetType.text = parent.context.getString(adapterTitle)
    }
    return FleetViewHolder(binding)
  }

  override fun onBindViewHolder(
    holder: FleetViewHolder,
    position: Int
  ) {
    val item = getItem(position)
    holder.bind(item)

    if (itemCount == 0) {
      emptyState.invoke(true)
      return
    }

    with(holder.binding) {
      fleetContainer.setOnClickListener { onViewHolderCreated(item) }
      moreButton.setOnClickListener { popUpDelete(it, position, item) }
      fleetType.isVisible = position == 0 && itemCount > 0
    }

  }

  private fun popUpDelete(view: View, position: Int, item: T) {
    PopupMenu(view.context, view).apply {
      gravity = Gravity.END
      menuInflater.inflate(R.menu.popup_fleet_delete_menu, menu)
      view.context.setActiveState(item.isActive, menu.findItem(R.id.delivery_state))
      setOnMenuItemClickListener {
        if (it.itemId == R.id.delete) {
          onDeleteFleet(getItem(position).id)
        }
        if (it.itemId == R.id.delivery_state) {
            val invertedState = !item.isActive
            onChangeActiveState(item.id, invertedState)
        }
        return@setOnMenuItemClickListener true
      }
    }.show()
  }

  private fun Context.setActiveState(isActive: Boolean, menuItem: MenuItem) {
    val invertState = isActive then "inactive" ?: "active"
    val state = getString(R.string.set_state, invertState)
    menuItem.title = state
  }

  abstract fun onViewHolderCreated(data: T)
  abstract fun onDeleteFleet(id: String)
  abstract fun onChangeActiveState(id: String, activeState: Boolean)

  class FleetViewHolder(
    val binding: FleetItemBinding
  ) : ViewHolder(binding.root) {

    fun <T : FleetUiModel> bind(
      data: T
    ) = with(binding) {
      if (data is FleetUiModel.VehicleUri) {
        name.text = data.name
        plateNumber.text = data.plate

        val capacity = "${data.loadCapacity} kg"
        maxCapacity.text = capacity
      }

      if (data is FleetUiModel.DriverUri) {
        name.text = data.fullName
        plateNumber.isVisible = false
        maxCapacity.isVisible = false
        dash.isVisible = false
      }

      if (data.profile != "null") {
        bindImageWith(fleetIcon, Uri.parse(data.profile))
      }

      if (data.hasOngoingDeliveries) {
        availabilityIndicator.setNotAvailable()
      } else {
        availabilityIndicator.setActiveStateIndicatorColor(
          (data as? FleetUiModel.VehicleUri)?.isActive
            ?: (data as FleetUiModel.DriverUri).isActive
        )
      }
    }
  }
}