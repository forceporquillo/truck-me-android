package dev.forcecodes.truckme.ui.dashboard

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.domain.dashboard.UpdateFleetStateId
import dev.forcecodes.truckme.core.domain.dashboard.DeliveryItems
import dev.forcecodes.truckme.databinding.DeliveryItemBinding
import dev.forcecodes.truckme.extensions.bindProfileIcon

class DeliveryAdapter : ListAdapter<DeliveryItems, DeliveryViewHolder>(COMPARATOR) {

  var onActiveJobClick: (String) -> Unit = {}
  var onDeleteJob: (UpdateFleetStateId) -> Unit = {}

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): DeliveryViewHolder {
    return DeliveryViewHolder(
      DeliveryItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      ),
      onActiveJobClick,
      onDeleteJob
    )
  }

  override fun onBindViewHolder(
    holder: DeliveryViewHolder,
    position: Int
  ) {
    val item = getItem(position)
    holder.bind(item)
  }

  companion object {

    private val COMPARATOR = object : DiffUtil.ItemCallback<DeliveryItems>() {
      override fun areItemsTheSame(
        oldItem: DeliveryItems,
        newItem: DeliveryItems
      ) = oldItem.id == newItem.id

      override fun areContentsTheSame(
        oldItem: DeliveryItems,
        newItem: DeliveryItems
      ) = oldItem == newItem
    }
  }
}

class DeliveryViewHolder(
  private val binding: DeliveryItemBinding,
  onActiveJobClick: (String) -> Unit = {},
  private val onDeleteJob: (UpdateFleetStateId) -> Unit = {}
) : RecyclerView.ViewHolder(binding.root) {

  private var clearFleetState: UpdateFleetStateId? = null

  init {
    binding.root.setOnClickListener {
      clearFleetState?.documentId?.let(onActiveJobClick)
    }
    binding.dragIcon.setOnClickListener(::popUpDelete)
  }

  private fun popUpDelete(view: View) {
    PopupMenu(view.context, view).apply {
      gravity = Gravity.END
      menuInflater.inflate(R.menu.popup_job_delete_menu, menu)
      setOnMenuItemClickListener {
        if (it.itemId == R.id.delete_state) {
          clearFleetState?.let(onDeleteJob)
        }
        return@setOnMenuItemClickListener true
      }
    }.show()
  }

  fun bind(items: DeliveryItems) {
    clearFleetState = UpdateFleetStateId(items.id, items.driverId, items.vehicleId)
    with(binding) {
      deliverTo.text = items.driverName
      destination.text = items.destination
      eta.text = items.eta

      if (!items.profileIcon.isNullOrEmpty()) {
        profileIcon.bindProfileIcon(items.profileIcon)
      }
    }
  }
}