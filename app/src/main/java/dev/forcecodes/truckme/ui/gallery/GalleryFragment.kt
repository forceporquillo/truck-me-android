package dev.forcecodes.truckme.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.BottomSheetGalleryFragmentBinding
import dev.forcecodes.truckme.extensions.setupToolbarPopBackStack
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Alternative gallery selector when permission hasn't granted or ignored.
 */
abstract class GalleryFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onImageSelectedResult(uri)
        }
    }

    protected fun launchGallery() {
        getContent.launch("image/*")
    }

    abstract fun onImageSelectedResult(imageUri: Uri)
}





