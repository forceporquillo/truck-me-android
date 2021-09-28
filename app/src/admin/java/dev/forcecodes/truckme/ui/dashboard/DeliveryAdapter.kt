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

  var onActiveJobClick: () -> Unit = {}

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): DeliveryViewHolder {
    return DeliveryViewHolder(
      DeliveryItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      ).also { binding ->
        binding.root.setOnClickListener { onActiveJobClick.invoke() }
      }
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
  private val binding: DeliveryItemBinding
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(items: DeliveryItems) {
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