package dev.forcecodes.truckme.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.forcecodes.truckme.core.model.Places
import dev.forcecodes.truckme.databinding.PlaceDirectionItemBinding
import dev.forcecodes.truckme.ui.search.SearchPlacesAdapter.SearchViewHolder

class SearchPlacesAdapter : ListAdapter<Places, SearchViewHolder>(SEARCH_COMPARATOR) {

  var onPlaceClickListener: ((Places) -> Unit)? = null

  companion object {
    private val SEARCH_COMPARATOR = object : DiffUtil.ItemCallback<Places>() {
      override fun areItemsTheSame(
        oldItem: Places,
        newItem: Places
      ): Boolean {
        return oldItem.placeId == newItem.placeId
      }

      @SuppressLint("DiffUtilEquals")
      override fun areContentsTheSame(
        oldItem: Places,
        newItem: Places
      ): Boolean {
        return oldItem == newItem
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = PlaceDirectionItemBinding.inflate(inflater, parent, false)
    return SearchViewHolder(binding)
  }

  override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
    val places = getItem(position)
    holder.bindPlace(places)

    holder.binding.root.setOnClickListener {
      onPlaceClickListener?.invoke(places)
    }
  }

  class SearchViewHolder(
    val binding: PlaceDirectionItemBinding
  ) : ViewHolder(binding.root) {

    fun bindPlace(place: Places) {
      binding.title.text = place.title
      binding.address.text = place.address
    }
  }
}

