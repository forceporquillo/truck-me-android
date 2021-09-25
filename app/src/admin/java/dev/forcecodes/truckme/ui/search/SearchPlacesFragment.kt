package dev.forcecodes.truckme.ui.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.truckme.MainActivity
import dev.forcecodes.truckme.R
import dev.forcecodes.truckme.core.util.then
import dev.forcecodes.truckme.databinding.FragmentSearchPlacesBinding
import dev.forcecodes.truckme.databinding.SearchViewBinding
import dev.forcecodes.truckme.extensions.navigateUp
import dev.forcecodes.truckme.extensions.repeatOnLifecycleParallel
import dev.forcecodes.truckme.extensions.viewBinding
import dev.forcecodes.truckme.ui.dashboard.mapNavGraphViewModels
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPlacesFragment : Fragment(R.layout.fragment_search_places),
  SearchView.OnQueryTextListener {

  private val searchPlacesViewModel by viewModels<SearchPlacesViewModel>()
  private val mapDeliveryViewModel by mapNavGraphViewModels()

  private val binding by viewBinding(FragmentSearchPlacesBinding::bind)

  private var _searchBinding: SearchViewBinding? = null
  private val searchBinding get() = _searchBinding!!

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = searchPlacesViewModel

    val adapter = SearchPlacesAdapter()
    initAdapter(adapter)
    initSearchView()

    repeatOnLifecycleParallel {
      launch {
        searchPlacesViewModel.placesResults.collect(adapter::submitList)
      }
      launch {
        searchPlacesViewModel.isLoading.collect(::setLoading)
      }
    }
    adapter.onPlaceClickListener = { places ->
      mapDeliveryViewModel.addSelectedDestination(places)
      navigateUp()
    }
  }

  private fun initAdapter(adapter: SearchPlacesAdapter) {
    binding.recyclerview.adapter = adapter
    val dividerItemDecoration =
      DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
    binding.recyclerview.addItemDecoration(dividerItemDecoration)
  }

  private fun initSearchView() {
    val toolbar = activityToolbar()
    _searchBinding = SearchViewBinding.inflate(layoutInflater, toolbar, true)
    searchBinding.root.setOnQueryTextListener(this)
  }

  private fun setLoading(isLoading: Boolean) {
    binding.progressIndicator.alpha = isLoading then 1f ?: 0f
  }

  override fun onQueryTextChange(newText: String?): Boolean {
    searchPlacesViewModel.searchPlace(newText ?: "")
    return true
  }

  override fun onQueryTextSubmit(query: String?): Boolean {
    searchPlacesViewModel.searchPlace(query ?: "")
    return true
  }

  override fun onDestroyView() {
    super.onDestroyView()
    activityToolbar().removeView(searchBinding.root)
    _searchBinding = null
  }

  private fun activityToolbar(): MaterialToolbar {
    return (requireActivity() as MainActivity).getToolbar()
  }
}

