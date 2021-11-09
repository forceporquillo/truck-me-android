package dev.forcecodes.truckme.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.forcecodes.truckme.extensions.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber

abstract class GalleryFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  private val galleryViewModel by viewModels<GalleryViewModel>()

  private var profileIcon: ImageView? = null
  private var avatar: ImageView? = null
  private var imageButton: CardView? = null

  abstract fun onProfileChange(profileInBytes: ByteArray)
  abstract fun requireGalleryViews(): GalleryViews

  private var getContent: ActivityResultLauncher<String>? = null

  data class GalleryViews(
    val profileIcon: ImageView,
    val avatar: ImageView,
    val button: CardView
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
      galleryViewModel.setImageUri(uri ?: return@registerForActivityResult)
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
  }

  override fun onStart() {
    super.onStart()
    imageButton?.setOnClickListener {
      getContent?.launch("image/*")
    }
  }

  override fun onResume() {
    super.onResume()
    observeOnLifecycleStarted {
      galleryViewModel.imageUri.collect {
        bindProfile(it ?: return@collect)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    galleryViewModel.setImageUri(null)
  }

  private fun <T> bindProfile(urlOrUri: T) {
    bindProfileIcon(urlOrUri, true) { profileInBytes ->
      onProfileChange(profileInBytes)
    }
  }

  abstract fun onImageSelectedResult(imageUri: Uri)

  protected fun <T> bindProfileIcon(
    data: T,
    dispatch: Boolean = true,
    block: ((ByteArray) -> Unit)? = null
  ) {
    avatar?.isGone = true

    profileIcon?.bindProfileIcon(data) isReady@{ isReady ->
      avatar?.isGone = isReady

      Timber.e(isReady.toString())
      if (!isReady) {
        return@isReady
      }

      if (dispatch) {
        Timber.e("saving...")
        // save profile bitmap everytime glide returns true
        saveCompressedBitmap { profileInBytes ->
          block?.invoke(profileInBytes)
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

  override fun onDestroyView() {
    super.onDestroyView()
    profileIcon = null
    avatar = null
    imageButton = null
  }
}





