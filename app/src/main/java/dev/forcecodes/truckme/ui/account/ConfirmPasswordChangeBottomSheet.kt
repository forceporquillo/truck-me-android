package dev.forcecodes.truckme.ui.account

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.base.BaseBottomSheetDialogFragment
import dev.forcecodes.truckme.databinding.BottomSheetConfirmPasswordChangeBinding
import dev.forcecodes.truckme.extensions.observeWithOnRepeatLifecycle
import dev.forcecodes.truckme.extensions.postRunnable
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.extensions.viewBinding
import kotlinx.coroutines.flow.collect

class ConfirmPasswordChangeBottomSheet : BaseBottomSheetDialogFragment(R.layout.bottom_sheet_confirm_password_change) {

  companion object {
    const val TAG = "ConfirmPasswordChangeBottomSheet"
  }

  private val viewModel by viewModels<AccountSettingsViewModel>({ requireParentFragment() })
  private val binding by viewBinding(BottomSheetConfirmPasswordChangeBinding::bind)

  private var oldPassword: String? = ""
  private var isDirty = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    setShowListener()

    binding.submit.setOnClickListener {
      postRunnable {
        viewModel.changePasswordClick()
        dismiss()
      }
    }

    observeWithOnRepeatLifecycle {
      viewModel.oldPasswordInvalid.collect {
        oldPassword = it.oldPassword
        binding.oldPassword.error = it.errorMessage
        binding.oldPasswordEt.setText(it.oldPassword)
      }
    }

    // observe text changes and consider it as a
    // dirty flag to clear out the error message.
    binding.oldPasswordEt.textChangeObserver { value ->
      if (oldPassword == null) {
        return@textChangeObserver
      }

      if (isDirty) {
        return@textChangeObserver
      }

      if (oldPassword != value) {
        binding.oldPassword.error = ""
        isDirty = true
      }
    }
  }

  private fun setShowListener() {
    val dialogListener: ((DialogInterface) -> Unit) = {
      postRunnable {
        val bottomSheet = (dialog as? BottomSheetDialog)
          ?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
        bottomSheet?.let { frame ->
          BottomSheetBehavior.from(frame).state = BottomSheetBehavior.STATE_EXPANDED
        }
      }
    }
    dialog?.setOnShowListener(dialogListener)
  }
}