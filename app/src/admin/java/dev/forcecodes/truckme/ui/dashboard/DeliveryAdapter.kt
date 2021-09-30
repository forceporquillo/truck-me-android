package dev.forcecodes.truckme.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.core.domain.dashboard.DeliveryItems
import dev.forcecodes.truckme.databinding.DeliveryItemBinding
import dev.forcecodes.truckme.extensions.bindProfileIcon

class DeliveryAdapter : ListAdapter<DeliveryItems, DeliveryViewHolder>(COMPARATOR) {

  var onActiveJobClick: (String) -> Unit = {}

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
      onActiveJobClick
    )
  }

  override fun onBindViewHolder(
    holder: DeliveryViewHolder,
    position: Int
  ) {
    holder.bind(getItem(position))
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
  private val onActiveJobClick: (String) -> Unit = {}
) : RecyclerView.ViewHolder(binding.root) {

  private lateinit var itemId: String

  init {
    binding.root.setOnClickListener {
      onActiveJobClick(itemId)
    }
  }

  fun bind(items: DeliveryItems) {
    itemId = items.id
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