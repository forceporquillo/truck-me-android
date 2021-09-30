package dev.forcecodes.truckme.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.forcecodes.truckme.extensions.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class GalleryFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  private lateinit var getContent: ActivityResultLauncher<String>

  private val imageViewModel by viewModels<GalleryViewModel>()

  private var galleryBottomSheet: GalleryBottomSheet? = null

  private var profileIcon: ImageView? = null
  private var avatar: ImageView? = null
  private var imageButton: CardView? = null

  abstract fun onProfileChange(profileInBytes: ByteArray)
  abstract fun requireGalleryViews(): GalleryViews

  data class GalleryViews(
    val profileIcon: ImageView,
    val avatar: ImageView,
    val button: CardView
  )

  protected var imageUrl: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      onImageSelectedResult(uri)
      //  imageViewModel.setImageUri(uri)
    }
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    val (profileIcon, avatar, button) = requireGalleryViews()

    this.profileIcon = profileIcon
    this.avatar = avatar
    this.imageButton = button

    repeatOnLifecycleParallel {
      launch {
        imageViewModel.image.collect { image: Image? ->
          image ?: return@collect
          bindProfile(image.imageUri ?: image.imageUrl)
        }
      }

      launch {
        imageViewModel.imageUri.collect { uri: Uri? ->
          uri?.let { bindProfile(it) }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    imageButton?.setOnClickListener(::showBottomSheetGallery)
    if (imageUrl != null) {
      imageViewModel.selectedImage(Image(null, imageUrl))
    }
  }

  private fun <T> bindProfile(urlOrUri: T) {
    bindProfileIcon(urlOrUri) { profileInBytes ->
      onProfileChange(profileInBytes)
    }
  }

  protected fun launchGallery() {
    getContent.launch("image/*")
  }

  abstract fun onImageSelectedResult(imageUri: Uri)

  private fun showBottomSheetGallery(view: View) {
    galleryBottomSheet = GalleryBottomSheet()
    view.disableButtonForAWhile {
      galleryBottomSheet?.show(childFragmentManager, GalleryBottomSheet.GALLERY_TAG)
    }
    view.requestFocus()
  }

  protected fun <T> bindProfileIcon(
    data: T,
    dispatch: Boolean = true,
    block: ((ByteArray) -> Unit)? = null
  ) {
    profileIcon?.bindProfileIcon(data) isReady@{ isReady ->
      avatar?.isGone = isReady

      Timber.e(isReady.toString())
      if (!isReady) {
        return@isReady
      }

      if (dispatch) {
        // save profile bitmap everytime glide returns true
        saveCompressedBitmap { profileInBytes ->
          block?.invoke(profileInBytes)
        }
        galleryBottomSheet?.let { bottomSheet ->
          if (bottomSheet.isAdded) {
            bottomSheet.dismiss()
          }
        }
      }
    }
  }

  private fun saveCompressedBitmap(block: (ByteArray) -> Unit) {
    // Suppress exception by placing our code to a runnable queue
    // and wait 'til Glide processes the image from the separate thread.
    with(profileIcon) {
      this?.post {
        block(compressAsBitmap())
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    getContent.unregister()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    profileIcon = null
    avatar = null
    imageButton = null
  }
}





