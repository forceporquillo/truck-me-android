package dev.forcecodes.truckme.ui.fleet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.databinding.FleetItemBinding
import dev.forcecodes.truckme.databinding.FragmentFleetBinding
import dev.forcecodes.truckme.extensions.updateMarginParams
import dev.forcecodes.truckme.extensions.viewBinding

class FleetFragment : Fragment(R.layout.fragment_fleet), FleetItemListener {

    private val binding by viewBinding(FragmentFleetBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fleetList.adapter = FleetAdapter(this)
        binding.vehicleFleetList.adapter = FleetAdapter(this)
    }

    override fun onItemClick() {
        findNavController().navigate(R.id.action_fleetFragment_to_addDriverFragment)
    }
}

interface FleetItemListener {
    fun onItemClick()
}

class FleetAdapter(private val listener: FleetItemListener) :
    RecyclerView.Adapter<FleetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FleetViewHolder {
        return FleetViewHolder(
            FleetItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FleetViewHolder, position: Int) {
        if (position == 9) {
            holder.itemView.updateMarginParams {
                val resource = holder.itemView.context.resources
                bottomMargin = resource.getDimensionPixelOffset(R.dimen.spacing_extra_small)
            }
        }
        holder.itemView.setOnClickListener { listener.onItemClick() }
    }

    override fun getItemCount(): Int {
        return 10
    }
}

class FleetViewHolder(
    private val binding: FleetItemBinding
) : RecyclerView.ViewHolder(binding.root) {

}