package dev.forcecodes.truckme.ui.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryViewModel : ViewModel() {

    private val _image = MutableStateFlow<Image?>(null)
    val image = _image.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    fun selectedImage(image: Image) {
        _image.value = image
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }
}