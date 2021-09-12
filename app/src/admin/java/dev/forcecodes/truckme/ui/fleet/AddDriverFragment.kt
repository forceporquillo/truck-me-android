package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentAddDriverBinding
import dev.forcecodes.truckme.extensions.viewBinding

class AddDriverFragment : Fragment(R.layout.fragment_add_driver) {

    private val binding by viewBinding(FragmentAddDriverBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}