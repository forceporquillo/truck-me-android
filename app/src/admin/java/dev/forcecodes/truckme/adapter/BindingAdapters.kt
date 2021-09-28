package dev.forcecodes.truckme.adapter

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import dev.forcecodes.truckme.extensions.textChangeObserver
import dev.forcecodes.truckme.ui.dashboard.MapDeliverySharedViewModel

@BindingAdapter("items")
fun TextInputEditText.itemsListener(viewModel: MapDeliverySharedViewModel) {
  textChangeObserver { viewModel.freightItem(it) }
}

@BindingAdapter("destination")
fun TextInputEditText.destination(viewModel: MapDeliverySharedViewModel) {
  textChangeObserver { viewModel.destinationAddress(it) }
}

@BindingAdapter("contact")
fun TextInputEditText.contact(viewModel: MapDeliverySharedViewModel) {
  textChangeObserver { viewModel.contact(it) }
}