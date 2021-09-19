package dev.forcecodes.truckme.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.databinding.GalleryItemBinding

internal class GalleryAdapter(private val onClickListener: ImageClickListener<Image>) :
  ListAdapter<Image, GalleryViewHolder>(COMPARATOR) {

  companion object {
    private val COMPARATOR = object : DiffUtil.ItemCallback<Image>() {
      override fun areItemsTheSame(
        oldItem: Image,
        newItem: Image
      ): Boolean {
        return oldItem.imageUri == newItem.imageUri
      }

      override fun areContentsTheSame(
        oldItem: Image,
        newItem: Image
      ) = true
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): GalleryViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = GalleryItemBinding.inflate(inflater, parent, false)
    return GalleryViewHolder(binding, onClickListener)
  }

  override fun onBindViewHolder(
    holder: GalleryViewHolder,
    position: Int
  ) {
    holder.bindUri(getItem(position))
  }
}

interface ImageClickListener<T> {
  fun onImageSelected(data: T)
}

internal class GalleryViewHolder(
  private val binding: GalleryItemBinding,
  private val clickListener: ImageClickListener<Image>
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

  private var image: Image? = null

  init {
    binding.root.setOnClickListener(this)
  }

  fun bindUri(image: Image) {
    this.image = image
    binding.image = image
    // no need to set lifecycleOwner
  }

  override fun onClick(p0: View?) {
    image?.let { clickListener.onImageSelected(it) }
  }
}
