package dev.forcecodes.truckme.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.BottomSheetGalleryFragmentBinding
import dev.forcecodes.truckme.extensions.setupToolbarPopBackStack
import dev.forcecodes.truckme.extensions.withToolbarElevationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// freebie
@AndroidEntryPoint
class GalleryBottomSheet : BottomSheetDialogFragment(), ImageClickListener<Image> {

    companion object {
        const val GALLERY_TAG = "GalleryBottomSheet"
    }

    @Inject
    lateinit var imageGalleryLocator: ImageGalleryLocator

    @Inject
    lateinit var galleryItemDecoration: GalleryItemDecoration

    private val viewModel by viewModels<GalleryViewModel>({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_gallery_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(BottomSheetGalleryFragmentBinding.bind(view)) {
            toolbar.setupToolbarPopBackStack(::dismiss)
            setGalleryList(galleryList)
        }
    }

    private fun setGalleryList(galleryList: RecyclerView) {
        with(galleryList) {
            addItemDecoration(galleryItemDecoration)
            adapter = GalleryAdapter(this@GalleryBottomSheet).also {
                imageGalleryLocator.getAllImages(it::submitList)
            }
        }
    }

    override fun onImageSelected(data: Image) {
        viewModel.selectedImage(data)
    }
}

class GalleryViewModel : ViewModel() {

    private val _imageUri = MutableStateFlow<Image?>(null)
    val image = _imageUri.asStateFlow()

    fun selectedImage(image: Image) {
        _imageUri.value = image
    }
}