package dev.forcecodes.truckme.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.core.data.ActiveJobItems
import dev.forcecodes.truckme.databinding.AssignedJobItemBinding
import dev.forcecodes.truckme.ui.AssignedJobAdapter.AssignedViewHolder

class AssignedJobAdapter : ListAdapter<ActiveJobItems, AssignedViewHolder>(JOBS_COMPARATOR) {

  var onAssignedJobClick: (String) -> Unit = {}

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): AssignedViewHolder {
    return AssignedViewHolder(
      AssignedJobItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      ),
      onAssignedJobClick
    )
  }

  override fun onBindViewHolder(
    holder: AssignedViewHolder,
    position: Int
  ) {
    holder.bind(getItem(position))
  }

  class AssignedViewHolder(
    private val binding: AssignedJobItemBinding,
    private val onAssignedJobClick: (String) -> Unit = {}
  ) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var jobId: String

    init {
      binding.root.setOnClickListener {
        onAssignedJobClick(jobId)
      }
    }

    fun bind(item: ActiveJobItems) {
      jobId = item.id
      with(binding) {
        deliverTv.text = item.title
        destinationTv.text = item.destination
        itemTv.text = item.items
      }
    }
  }

  companion object {
    private val JOBS_COMPARATOR = object : DiffUtil.ItemCallback<ActiveJobItems>() {
      override fun areItemsTheSame(
        oldItem: ActiveJobItems,
        newItem: ActiveJobItems
      ) = oldItem.id == newItem.id

      override fun areContentsTheSame(
        oldItem: ActiveJobItems,
        newItem: ActiveJobItems
      ) = oldItem == newItem
    }
  }
}