package dev.forcecodes.truckme.ui.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.R.string
import dev.forcecodes.truckme.extensions.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

private const val REQUEST_CODE_CHOOSE = 0x1377

abstract class GalleryFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

  private val galleryViewModel by viewModels<GalleryViewModel>()

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
      launchGallery()
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

  private fun <T> bindProfile(urlOrUri: T) {
    bindProfileIcon(urlOrUri, true) { profileInBytes ->
      onProfileChange(profileInBytes)
    }
  }

  @Suppress("deprecation")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
      val imageUri = Matisse.obtainResult(data)
      galleryViewModel.setImageUri(imageUri[0])
    }
  }

  private fun launchGallery() {
    Matisse.from(this)
      .choose(MimeType.ofAll())
      .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
      .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
      .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
      .thumbnailScale(0.85f)
      .imageEngine(GlideEngine())
      .showPreview(true) // Default is `true`
      .maxOriginalSize(10)
      .originalEnable(true)
      .theme(R.style.PickerGalleryStyle)
      .autoHideToolbarOnSingleTap(true)
      .forResult(REQUEST_CODE_CHOOSE)
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

  internal class GifSizeFilter(
    private val mMinWidth: Int,
    private val mMinHeight: Int,
    private val mMaxSize: Int
  ) : Filter() {
    public override fun constraintTypes(): HashSet<MimeType?> {
      return object : HashSet<MimeType?>() {
        init {
          add(MimeType.GIF)
        }
      }
    }

    override fun filter(context: Context, item: Item): IncapableCause? {
      if (!needFiltering(context, item)) return null
      val size: Point =
        PhotoMetadataUtils.getBitmapBound(context.contentResolver, item.contentUri)
      return if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
        IncapableCause(
          IncapableCause.DIALOG,
          context.getString(
            string.error_gif,
            mMinWidth,
            PhotoMetadataUtils.getSizeInMB(mMaxSize.toLong()).toString()
          )
        )
      } else null
    }
  }
}





