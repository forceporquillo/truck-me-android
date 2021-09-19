package dev.forcecodes.truckme.ui.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageGalleryLocator @Inject constructor(@ApplicationContext private val context: Context) {

  companion object {
    private val URI_EXTERNAL = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
  }

  fun getAllImages(block: (List<Image>) -> Unit) {
    val listOfAllImages = mutableListOf<Image>()

    val cursor = context.contentResolver.query(
      URI_EXTERNAL, arrayOf(MediaStore.Images.Media._ID),
      null, null,
      "${MediaStore.Images.Media.DATE_TAKEN} DESC"
    )

    cursor?.let { c ->
      val columnIndexID = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
      while (c.moveToNext()) {
        val imageId = c.getLong(columnIndexID)
        val uriImage = Uri.withAppendedPath(URI_EXTERNAL, "" + imageId)
        listOfAllImages.add(Image(imageUri = uriImage))
        block(listOfAllImages)
      }
      c.close()
    }
  }
}
