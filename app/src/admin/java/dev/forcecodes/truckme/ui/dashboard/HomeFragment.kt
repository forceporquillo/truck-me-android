package dev.forcecodes.truckme.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FragmentHomeBinding
import dev.forcecodes.truckme.extensions.navigateOnButtonClick
import java.util.UUID

class HomeFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val binding = FragmentHomeBinding.bind(view)

    val deliveryAdapter = DeliveryAdapter()
    binding.deliveryList.adapter = deliveryAdapter

    binding.addButton.navigateOnButtonClick(R.id.to_map_nav_graph)

    deliveryAdapter.submitList(
      listOf(
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Kaha",
          "Warehouse - Fairview",
          "ETA: 7:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        ),
        DeliveryItems(
          UUID.randomUUID().toString(),
          System.currentTimeMillis().toString(),
          "Inbound raw materials",
          "Bulacan Warehouse",
          "ETA: 5:23 PM",
        )
      )
    )
  }
}





