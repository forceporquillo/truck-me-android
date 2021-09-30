package dev.forcecodes.truckme.ui.fleet

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.forcecodes.truckme.core.data.fleets.FleetUiModel
import dev.forcecodes.truckme.databinding.FleetItemBinding
import dev.forcecodes.truckme.extensions.bindImageWith
import dev.forcecodes.truckme.extensions.setActiveStateIndicatorColor

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
    holder.bind(getItem(position))

    if (itemCount == 0) {
      emptyState.invoke(true)
      return
    }
    holder.binding.fleetContainer.setOnClickListener {
      onViewHolderCreated(getItem(position))
    }
    holder.binding.fleetType.isVisible = position == 0 && itemCount > 0
  }

  abstract fun onViewHolderCreated(data: T)

  class FleetViewHolder(
    val binding: FleetItemBinding
  ) : ViewHolder(binding.root) {

    fun <T : FleetUiModel> bind(
      data: T
    ) = with(binding) {
      if (data is FleetUiModel.VehicleUri) {
        name.text = data.name
        plateNumber.text = data.plate
      }

      if (data is FleetUiModel.DriverUri) {
        name.text = data.fullName
        plateNumber.isVisible = false
      }

      if (data.profile != "null") {
        bindImageWith(fleetIcon, Uri.parse(data.profile))
      }

      availabilityIndicator.setActiveStateIndicatorColor(
        (data as? FleetUiModel.VehicleUri)?.isActive
          ?: (data as FleetUiModel.DriverUri).isActive
      )
    }
  }
}