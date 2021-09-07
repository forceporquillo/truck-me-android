package dev.forcecodes.truckme.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AuthToolbarVisibilityListener) {
            context.onShowToolbar(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentResetPasswordBinding.bind(view)
    }

    override fun onDestroyView() {
        (context as? AuthToolbarVisibilityListener)?.onShowToolbar(false)
        super.onDestroyView()
    }
}