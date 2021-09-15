package dev.forcecodes.truckme.ui.gallery

import android.net.Uri

data class Image(val imageUri: Uri? = null, val imageUrl: String? = null) {
    override fun toString(): String {
        return "Image(imageUri=$imageUri, imageUrl=$imageUrl)"
    }
}